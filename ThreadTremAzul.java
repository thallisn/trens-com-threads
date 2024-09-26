/* ***************************************************************
* Autor............: Thallis Luciano Curcino Nunes
* Matricula........: 202211065
* Inicio...........: 21/08/2023
* Ultima alteracao.: 07/10/2023
* Nome.............: Programacao Concorrente - Trabalho 02.
* Funcao...........: O programa busca explorar os conceitos da 
*                    programacao concorrente utilizando threads
*                    e a exclusao mutua para evitar os efeitos  
*                    das condicoes de corrida durante o acesso
*                    aos trilhos compartilhados. Tres solucoes 
*                    foram implementadas para resolver (ou minimi
*                    zar) a colisao dos trens.
*************************************************************** */

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/* ****************************************************************
* Classe: ThreadTremAzul
* Funcao: Representar o Trem Azul e seus comportamentos na tela.
* Descricao: A classe utiliza uma thread para a representar o Trem
*            Amarelo.
*            Os seus metodos e variaveis sao utilizados para simular
*            a movimentacao do trem pelos trilhos e para controlar o
*            acesso ao recurso compartilhado (trilho unico) atraves
*            de interacoes com a classe controladora da tela (Tela
*            Principal). 
***************************************************************** */
public class ThreadTremAzul extends Thread {
  // Atributos
  private ImageView imageViewTremAzul;
  private Image imagemNoLadoEsquerdo;
  private Image imagemNoLadoDireito;
  private int velocidade = 8;// tempo (milisegundos) com que a thread faz mudancas na interface
  private int posicao_X_Atual;
  private int posicao_Y_Atual;
  private TelaPrincipal telaPrincipal;// classe que controla o acesso as RCs
  private boolean isParado = false;// indica se o trem esta em movimento ou nao
  private boolean isInterrompida = false;// indica se thread foi interrompida
  private int vezDoTremAzul = 1;// vez de executar a regiao critica(estrita alternancia)
  private int idTremAzul = 1;// posicao no vetor de interesse referente ao trem(solucao de peterson)

  // Construtor da classe
  public ThreadTremAzul(TelaPrincipal telaPrincipal, ImageView imageViewTremAzul, Image imagemNoLadoEsquerdo,
      Image imagemNoLadoDireito) {
    super("ThreadTremAzul");// define o nome da thread
    this.telaPrincipal = telaPrincipal;
    this.imageViewTremAzul = imageViewTremAzul;
    this.imagemNoLadoEsquerdo = imagemNoLadoEsquerdo;
    this.imagemNoLadoDireito = imagemNoLadoDireito;
  }// fim do construtor da classe

  @Override
  public void run() {// acoes a serem tomadas ao iniciar a thread
    while (!isInterrompida) {// looping enquanto a thread nao foi interrompida

      switch (telaPrincipal.getSolucaoEscolhida()) {// comportamento de acordo solucao escolhida

        case (1): {// ESCOLHEU VARIAVEL DE TRAVEMENTO
          if (escolheuLadoEsquerdo()) {// se a posicao para o trem escolhida for o lado Esq

            // o trem ira se mover no sentido esquerda -> direita
            mudarParaLadoEsquerdoDaTela();// move a imagem para o lado esquerdo da tela

            // inicio da regiao nao critica 1
            andarDaEsquerdaParaDireitaNa_RNC_1();
            // fim da regiao nao critica 1

            // inicio da regiao critica 1
            verificarRC_1();// pode entrar na RC_1?
            telaPrincipal.ocupar_RC_1();// informa que entrou na RC_1
            andarDaEsquerdaParaDireitaNa_RC_1();// anda na RC_1
            telaPrincipal.deixar_RC_1();// informa que saiu da RC_1
            // fim da regiao critica 1

            // inicio da regiao nao critica 2
            andarDaEsquerdaParaDireitaNa_RNC_2();
            // fim da regiao nao critica 2

            // inicio da regiao critica 2
            verificarRC_2();// pode entrar na RC_2?
            telaPrincipal.ocupar_RC_2();// informa que entrou na RC_2
            andarDaEsquerdaParaDireitaNa_RC_2();// anda na RC_2
            telaPrincipal.deixar_RC_2();// informa que saiu da RC_2
            // inicio da regiao critica 2

            // inicio da regiao nao critica 3
            andarDaEsquerdaParaDireitaNa_RNC_3();
            // fim da regiao nao critica 3

            break;
          } else {// se a posicao para o trem escolhida for o lado Dir

            // o trem ira se mover no sentido direita -> esquerda (inverso para as regioes)
            mudarParaLadoDireitoDaTela();// move a imagem para o lado direito da tela

            // inicio da regiao nao critica 3
            andarDaDireitaParaEsquerdaNa_RNC_3();
            // fim da regiao nao critica 3

            // inicio da regiao critica 2
            verificarRC_2();// pode entrar na RC_2?
            telaPrincipal.ocupar_RC_2();// informa que entrou na RC_2
            andarDaDireitaParaEsquerdaNa_RC_2();// anda na RC_2
            telaPrincipal.deixar_RC_2();// informa que saiu da RC_2
            // inicio da regiao critica 2

            // inicio da regiao nao critica 2
            andarDaDireitaParaEsquerdaNa_RNC_2();
            // fim da regiao nao critica 2

            // inicio da regiao critica 1
            verificarRC_1();// pode entrar na RC_1?
            telaPrincipal.ocupar_RC_1();// informa que entrou na RC_1
            andarDaDireitaParaEsquerdaNa_RC_1();// anda na RC_1
            telaPrincipal.deixar_RC_1();// informa que saiu da RC_1
            // inicio da regiao critica 1

            // inicio da regiao nao critica 1
            andarDaDireitaParaEsquerdaNa_RNC_1();
            // fim da regiao nao critica 1

            break;
          } // fim do bloco if-else (posicao escolhida)

        } // fim do case 1 (variavel de travamento)

        case (2): {// ESCOLHEU ESTRITA ALTERNANCIA
          if (escolheuLadoEsquerdo()) {// se a posicao para o trem escolhida for o lado Esq

            // o trem ira se mover no sentido esquerda -> direita
            mudarParaLadoEsquerdoDaTela();// move a imagem para o lado esquerdo da tela

            // inicio da regiao nao critica 1
            andarDaEsquerdaParaDireitaNa_RNC_1();
            // fim da regiao nao critica 1

            // inicio da regiao critica 1
            verificarVezDeExecutar_RC_1();// eh minha vez de passar na RC_1?
            andarDaEsquerdaParaDireitaNa_RC_1();// anda na RC_1
            telaPrincipal.sinalizarVezDoProximoExecutar_RC_1();// informa que eh a vez do proximo passar na RC_1
            // fim da regiao critica 1

            // inicio da regiao nao critica 2
            andarDaEsquerdaParaDireitaNa_RNC_2();
            // fim da regiao nao critica 2

            // inicio da regiao critica 2
            verificarVezDeExecutar_RC_2();// eh a minha vez de passar na RC_2?
            andarDaEsquerdaParaDireitaNa_RC_2();// anda na RC_2
            telaPrincipal.sinalizarVezDoProximoExecutar_RC_2();// informa que eh a vez do proximo passar na RC_2
            // inicio da regiao critica 2

            // inicio da regiao nao critica 3
            andarDaEsquerdaParaDireitaNa_RNC_3();
            // fim da regiao nao critica 3

            break;
          } else {// se a posicao para o trem escolhida for o lado Dir

            // o trem ira se mover no sentido direita -> esquerda (inverso para as regioes)
            mudarParaLadoDireitoDaTela();// move a imagem para o lado direito da tela

            // inicio da regiao nao critica 3
            andarDaDireitaParaEsquerdaNa_RNC_3();
            // fim da regiao nao critica 3

            // inicio da regiao critica 2
            verificarVezDeExecutar_RC_2();// eh a minha vez de passar na RC_2?
            andarDaDireitaParaEsquerdaNa_RC_2();// anda na RC_2
            telaPrincipal.sinalizarVezDoProximoExecutar_RC_2();// informa que eh a vez do proximo passar na RC_2
            // inicio da regiao critica 2

            // inicio da regiao nao critica 2
            andarDaDireitaParaEsquerdaNa_RNC_2();
            // fim da regiao nao critica 2

            // inicio da regiao critica 1
            verificarVezDeExecutar_RC_1();// eh minha vez de passar na RC_1?
            andarDaDireitaParaEsquerdaNa_RC_1();// anda na RC_1
            telaPrincipal.sinalizarVezDoProximoExecutar_RC_1();// informa que eh a vez do proximo passar na RC_1
            // fim da regiao critica 1

            // inicio da regiao nao critica 1
            andarDaDireitaParaEsquerdaNa_RNC_1();
            // fim da regiao nao critica 1

            break;
          } // fim do bloco if-else (posicao escolhida)

        } // fim do case 2 (estrita alternancia)

        case (3): {// ESCOLHEU SOLUCAO DE PETERSON
          if (escolheuLadoEsquerdo()) {// se a posicao para o trem escolhida for o lado Esq

            // o trem ira se mover no sentido esquerda -> direita
            mudarParaLadoEsquerdoDaTela();// move a imagem para o lado esquerdo da tela

            // inicio da regiao nao critica 1
            andarDaEsquerdaParaDireitaNa_RNC_1();
            // fim da regiao nao critica 1

            // inicio da regiao critica 1
            telaPrincipal.ENTRAR_RC_1(idTremAzul);// sinaliza interesse e verifica o estado da RC_1
            andarDaEsquerdaParaDireitaNa_RC_1();// anda na RC_1
            telaPrincipal.DEIXAR_RC_1(idTremAzul);// informa que a RC_1 esta vazia

            // inicio da regiao nao critica 2
            andarDaEsquerdaParaDireitaNa_RNC_2();
            // fim da regiao nao critica 2

            // inicio da regiao critica 2
            telaPrincipal.ENTRAR_RC_2(idTremAzul);// sinaliza interesse e verifica o estado da RC_2
            andarDaEsquerdaParaDireitaNa_RC_2();// anda na RC_2
            telaPrincipal.DEIXAR_RC_2(idTremAzul);// informa que a RC_2 esta vazia
            // inicio da regiao critica 2

            // inicio da regiao nao critica 3
            andarDaEsquerdaParaDireitaNa_RNC_3();
            // fim da regiao nao critica 3

            break;
          } else {// se a posicao para o trem escolhida for o lado Dir

            // o trem ira se mover no sentido direita -> esquerda (inverso para as regioes)
            mudarParaLadoDireitoDaTela();// move a imagem para o lado direito da tela

            // inicio da regiao nao critica 3
            andarDaDireitaParaEsquerdaNa_RNC_3();
            // fim da regiao nao critica 3

            // inicio da regiao critica 2
            telaPrincipal.ENTRAR_RC_2(idTremAzul);// sinaliza interesse e verifica o estado da RC_2
            andarDaDireitaParaEsquerdaNa_RC_2();// anda na RC_2
            telaPrincipal.DEIXAR_RC_2(idTremAzul);// informa que a RC_2 esta vazia
            // inicio da regiao critica 2

            // inicio da regiao nao critica 2
            andarDaDireitaParaEsquerdaNa_RNC_2();
            // fim da regiao nao critica 2

            // inicio da regiao critica 1
            telaPrincipal.ENTRAR_RC_1(idTremAzul);// sinaliza interesse e verifica o estado da RC_1
            andarDaDireitaParaEsquerdaNa_RC_1();// anda na RC_1
            telaPrincipal.DEIXAR_RC_1(idTremAzul);// informa que a RC_1 esta vazia
            // fim da regiao critica 1

            // inicio da regiao nao critica 1
            andarDaDireitaParaEsquerdaNa_RNC_1();
            // fim da regiao nao critica 1

            break;
          } // fim do bloco if-else (solucao de peterson)
        } // fim do case 3 (solucao de peterson)
      }// fim do switch (solucaoEscolhida)
    } // fim do while
  }// fim do metodo run

  // METODOS PARA VARIAVEL DE TRAVAMENTO
  public void verificarRC_1() {// verifica se a regiao critica 1 esta vazia (anda ou espera)
    while (telaPrincipal.podeEntrarNa_RC_1() == 1) {// espera enquanto RC_1 esta ocupada
      verificarSeEstaParado();// verifica se o trem parou em movimento
      try {
        Thread.sleep(1);
      } catch (InterruptedException exc) {// se a thread foi interrompida
        interromper();// quebra o laco do metodo run()
        break;// sai o laco atual
      } // fim do try-catch
    } // fim do while
  }// fim do metodo verificarRC_1

  public void verificarRC_2() {// verifica se a regiao critica 2 esta vazia (anda ou espera)
    while (telaPrincipal.podeEntrarNa_RC_2() == 1) {// espera enquanto RC_2 esta ocupada
      verificarSeEstaParado();// verifica se o trem parou em movimento
      try {
        Thread.sleep(1);
      } catch (InterruptedException exc) {// se a thread foi interrompida
        interromper();// quebra o laco do metodo run()
        break;// sai o laco atual
      } // fim do try-catch
    } // fim do while
  }// fim do metodo verificarRC_2

  // METODOS PARA ESTRITA ALTENANCIA
  public synchronized void verificarVezDeExecutar_RC_1() {// espera ate a ser a vez do trem percorrer a RC_1
    while (telaPrincipal.vezDeExecutar_RC_1() != vezDoTremAzul) {// espera
      verificarSeEstaParado();// verifica se o trem parou em movimento
      try {
        Thread.sleep(1);
      } catch (InterruptedException exc) {// se a thread foi interrompida
        interromper();// quebra o laco do metodo run()
        break;// sai o laco atual
      } // fim do try-catch
    } // fim do while
    notifyAll();
  }// fim do metodo verificarVezDeExecutarA_RC_1

  public synchronized void verificarVezDeExecutar_RC_2() {// espera ate a ser a vez do trem percorrer a RC_2
    while (telaPrincipal.vezDeExecutar_RC_2() != vezDoTremAzul) {// espera
      verificarSeEstaParado();// verifica se o trem parou em movimento
      try {
        Thread.sleep(1);
      } catch (InterruptedException exc) {// se a thread foi interrompida
        interromper();// quebra o laco do metodo run()
        break;// sai o laco atual
      } // fim do try-catch
    } // fim do while
  }// fim do metodo verificarVezDeExecutar_RC_2

  public void mudarParaLadoEsquerdoDaTela() {// muda o trem para o lado esquerdo dos trilhos
    // muda a posicao de partida do trem
    posicao_X_Atual = 0;
    posicao_Y_Atual = 274;
    // muda o trem para o lado esquerdo dos trilhos
    Platform.runLater(() -> this.imageViewTremAzul.setImage(imagemNoLadoEsquerdo));// troca a imagem exibida
    Platform.runLater(() -> this.imageViewTremAzul.setLayoutX(posicao_X_Atual));// muda a posicao da imagem
    Platform.runLater(() -> this.imageViewTremAzul.setLayoutY(posicao_Y_Atual));// muda a posicao da imagem
  }// fim do metodo mudarParaLadoEsquerdoDaTela

  public void mudarParaLadoDireitoDaTela() {// muda o trem para o lado direito dos trilhos
    // muda a posicao de partida do trem
    posicao_X_Atual = 782;
    posicao_Y_Atual = 274;
    // muda o trem para o lado direito dos trilhos
    Platform.runLater(() -> this.imageViewTremAzul.setImage(imagemNoLadoDireito));// troca a imagem exibida
    Platform.runLater(() -> this.imageViewTremAzul.setLayoutX(posicao_X_Atual));
    Platform.runLater(() -> this.imageViewTremAzul.setLayoutY(posicao_Y_Atual));
  }// fim do metodo mudarParaLadoDireitoDaTela

  public boolean escolheuLadoEsquerdo() {
    if (telaPrincipal.getPosicaoDosTrensEscolhida() == 1 ||
        telaPrincipal.getPosicaoDosTrensEscolhida() == 3) {
      return (true);// esta no lado esquerdo da tela
    } // fim do if
    return (false);// esta no lado direito da tela
  }// fim do metodo escolheuLadoEsquerdo

  public void subir(int novaPosY) {// novaPosY eh o nova coordenada do trem na tela
    while (posicao_Y_Atual != novaPosY) {// anda no eixo Y decrementando o seu valor ate atingir a nova posicao
      verificarSeEstaParado();// verifica se o trem parou em movimento
      try {
        Thread.sleep(velocidade);
      } catch (InterruptedException e) {// se a thread for interrompida
        interromper();// quebra o laco do metodo run()
        break;// sai o laco atual
      } // fim do bloco try-catch
      posicao_Y_Atual--;// atualiza o Y atual
      Platform.runLater(() -> imageViewTremAzul.setLayoutY(posicao_Y_Atual));// atualiza a pos do trem na tela
    } // fim do while
  }// fim do metodo subir

  public void descer(int novaPosY) {// novaPosY eh o nova coordenada do trem na tela
    while (posicao_Y_Atual != novaPosY) {// anda no eixo Y incrementando o seu valor ate atingir a nova posicao
      verificarSeEstaParado();// verifica se o trem parou em movimento
      try {
        Thread.sleep(velocidade);
      } catch (InterruptedException e) {// se a thread for interrompida
        interromper();// quebra o laco do metodo run()
        break;// sai o laco atual
      } // fim do bloco try-catch
      posicao_Y_Atual++;// incrementa Y atual
      Platform.runLater(() -> imageViewTremAzul.setLayoutY(posicao_Y_Atual));// atualiza a pos do trem na tela
    } // fim do while
  }// fim do metodo descer

  public void moverParaDireita(int novaPosX) {// novaPosX eh o nova coordenada do trem na tela
    while (posicao_X_Atual != novaPosX) {// anda no eixo X incrementando o seu valor ate atingir a nova posicao
      verificarSeEstaParado();// verifica se o trem parou em movimento
      try {
        Thread.sleep(velocidade);
      } catch (InterruptedException e) {// se a thread for interrompida
        interromper();// quebra o laco do metodo run()
        break;// sai o laco atual
      } // fim do bloco try-catch
      posicao_X_Atual++;// aumenta o X atual
      Platform.runLater(() -> imageViewTremAzul.setLayoutX(posicao_X_Atual));// atualiza a pos do trem na tela
    } // fim do while
  }// fim do metodo moverParaDireita

  public void moverParaEsquerda(int novaPosX) {// novaPosX eh o nova coordenada do trem na tela
    while (posicao_X_Atual != novaPosX) {// anda no eixo X decrementando o seu valor ate atingir a nova posicao
      verificarSeEstaParado();// verifica se o trem parou em movimento
      try {
        Thread.sleep(velocidade);
      } catch (InterruptedException e) {// se a thread for interrompida
        interromper();// quebra o laco do metodo run()
        break;// sai o laco atual
      } // fim do bloco try-catch
      posicao_X_Atual--;// decrementa o X atual
      Platform.runLater(() -> imageViewTremAzul.setLayoutX(posicao_X_Atual));// atualiza a pos do trem na tela
    } // fim do while
  }// fim do metodo moverParaEsquerda

  public synchronized void aumentarVelocidade() {
    if (velocidade > 2) {// quanto menor a velocidade, maior a velocidade do trem
      velocidade -= 2;// diminui a espera da thread para o trem se mover mais rapido
      isParado = false;// indica que esta andando
      notify();// notifica essa mudanca
    } // fim do if
  }// fim do metodo

  public void diminuirVelocidade() {
    if (velocidade <= 16) {// quanto maior a velocidade, menor a velocidade do trem
      velocidade += 2;// aumenta a espera da thread para o trem se mover mais devagar
    } // fim do if
  }// fim do metodo diminuirVelocidade

  public synchronized void reiniciar() {// volta a posicao, estado e velocidade iniciais do trem
    interromper();// interrompe o laco do metodo run()
    if (isInterrompida) {// apos a interrupcao
      mudarParaLadoEsquerdoDaTela();// muda a posicao da imagem na tela
      setVelocidade(8);// define a velocidade inicial
      isParado = false;// ao reiniciar o trem estara se movendo
      isInterrompida = false;// permite outra chamada do metodo run()
    } // fim do if
  }// fim do metodo reiniciar

  public synchronized void parar() {
    isParado = true;// atualiza o estado do trem
  }// fim do metodo parar

  public synchronized void verificarSeEstaParado() {
    while (isParado) {// faz thread entrar em um loop de espera ate ao trem voltar a andar
      try {
        wait();
      } catch (InterruptedException e) {// se a thread for interrompida
        interromper();// quebra o laco do metodo run()
        break;// sai o laco atual
      } // fim do bloco try-catch
    } // fim do while
  }// fim do metodo verificarSeEstaParado

  public synchronized void interromper() {
    interrupt();// interrompe a thread
    isInterrompida = true;// indica que a thread foi interrompida para quebrar o laco do metodo run()
    notify();// notifica a mudanca
  }// fim do metodo interromper

  // TREM ANDANDO ESQUERDA -> DIREITA

  public void andarDaEsquerdaParaDireitaNa_RNC_1() {// movimentos realizados pelo trem na RNC_1 sentido(Esq -> Dir)
    moverParaDireita(100);
  }// fim do metodo andarDaEsquerdaParaDireitaNa_RNC_1

  public void andarDaEsquerdaParaDireitaNa_RC_1() {// movimentos realizados pelo trem na RC_1 sentido(Esq -> Dir)
    subir(242);
    moverParaDireita(260);
  }// fim do metodo andarDaEsquerdaParaDireitaNa_RC_1

  public void andarDaEsquerdaParaDireitaNa_RNC_2() {// movimentos realizados pelo trem na RNC_2 sentido(Esq -> Dir)
    descer(282);
    moverParaDireita(480);
  }// fim do metodo andarDaEsquerdaParaDireitaNa_RNC_2

  public void andarDaEsquerdaParaDireitaNa_RC_2() {// movimentos realizados pelo trem na RC_2 sentido(Esq -> Dir)
    subir(242);
    moverParaDireita(630);
  }// fim do metodo andarDaEsquerdaParaDireitaNa_RC_2

  public void andarDaEsquerdaParaDireitaNa_RNC_3() {// movimentos realizados pelo trem na RNC_3 sentido(Esq -> Dir)
    descer(276);
    moverParaDireita(900);
  }// fim do metodo andarDaEsquerdaParaDireitaNa_RNC_3

  // TREM ANDANDO DIREITA -> ESQUERDA

  public void andarDaDireitaParaEsquerdaNa_RNC_3() {// movimentos realizados pelo trem na RNC_3 sentido(Dir -> Esq)
    moverParaEsquerda(660);
  }// fim do metodo andarDaDireitaParaEsquerdaNa_RNC_3

  public void andarDaDireitaParaEsquerdaNa_RC_2() {// movimentos realizados pelo trem na RC_2 sentido(Dir -> Esq)
    subir(242);
    moverParaEsquerda(500);
  }// fim do metodo andarDaDireitaParaEsquerdaNa_RC_2

  public void andarDaDireitaParaEsquerdaNa_RNC_2() {// movimentos realizados pelo trem na RNC_2 sentido(Dir -> Esq)
    descer(282);
    moverParaEsquerda(290);
  }// fim do metodo andarDaDireitaParaEsquerdaNa_RNC_2

  public void andarDaDireitaParaEsquerdaNa_RC_1() {// movimentos realizados pelo trem na RC_1 sentido(Dir -> Esq)
    subir(242);
    moverParaEsquerda(120);
  }// fim do metodo andarDaDireitaParaEsquerdaNa_RC_1

  public void andarDaDireitaParaEsquerdaNa_RNC_1() {// movimentos realizados pelo trem na RNC_1 sentido(Dir -> Esq)
    descer(274);
    moverParaEsquerda(-120);
  }// fim do metodo andarDaDireitaParaEsquerdaNa_RNC_1

  public void setVelocidade(int velocidade) {
    this.velocidade = velocidade;
  }// fim do metodo setVelocidade

}// fim da classe ThreadTremAzul