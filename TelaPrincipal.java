
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
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

/* ****************************************************************
* Classe: TelaPrincipal
* Funcao: Controle dos eventos gerados durante a execucao aplicacao.
* Descricao: A classe referencia os elemetos graficos definidos
*            no arquivo .fxml, intancia e inicia as threads que repre
*            sentam os trens Amarelo e Azul exibidos durante 
*            execucao do programa e define a implementacao dos metodos
*            que controlam o acesso ao recurso comparitlhado.
*            Alem disso, a classe possui  metodos "listeners" que 
*            definem as acoes a serem executadas de acordo com as 
*            interacoes do usuario com os elementos que compoem a 
*            interface grafica do programa, botoes, sliders, etc...
***************************************************************** */
public class TelaPrincipal implements Initializable {

  // Botoes
  @FXML
  private ImageView btnPosicaoEsqDir;// botao que define a posicao dos trens: Amarelo = Esq / Azul = Dir
  @FXML
  private ImageView btnPosicaoDirDir;// botao que define a posicao dos trens: Amarelo = Dir / Azul = Dir
  @FXML
  private ImageView btnPosicaoEsqEsq;// botao que define a posicao dos trens: Amarelo = Esq / Azul = Esq
  @FXML
  private ImageView btnPosicaoDirEsq;// botao que define a posicao dos trens: Amarelo = Dir / Azul = Esq
  @FXML
  private Button btnIniciar;// botao para iniciar a simulacao dos trens
  @FXML
  private Button btnReiniciar;// botao para reiniciar a simulacao dos trens

  // Controles de velocidade
  @FXML
  private Slider ctrlVelocidadeAmarelo;// slider que controla a velocidade do trem amarelo
  @FXML
  private Slider ctrlVelocidadeAzul;// slider que controla a velocidade do trem azul

  // Imagens que reprenstam os Trens
  @FXML
  private ImageView imvTremAmarelo;// camada de imagem que reprensenta o trem amarelo
  @FXML
  private ImageView imvTremAzul;// camada de imagem que representa ao trem azul

  // Painel de velocidade
  @FXML
  private Label painelInfoAmarelo;// camada de texto que exibe velocidade do trem amarelo
  @FXML
  private Label painelInfoAzul;// camada de texto que exibe velocidade do trem azul

  // Menu de solucoes de colisao
  @FXML
  private MenuButton menuDeSolucoes;// botao de menu que possui a lista com as solucoes para a colisao possiveis
  @FXML
  private MenuItem btnVarDeTravamento;
  @FXML
  private MenuItem btnEstritaAlternacia;
  @FXML
  private MenuItem btnSolDePeterson;
  @FXML
  private Label painelInfoColisao;// painel que exibe a solucao aplicada a simulacao

  // Imagens que representam os trens em ambos os lados da tela "Esq e Dir"
  private Image imgEsqTrmAmarelo = new Image("img/Trem_amarelo.png");
  private Image imgDirTrmAmarelo = new Image("img/Trem_amarelo_espelhado.png");
  private Image imgEsqTrmAzul = new Image("img/Trem_azul.png");
  private Image imgDirTrmAzul = new Image("img/Trem_azul_espelhado.png");

  // Threads que representam os trens na tela
  private ThreadTremAmarelo threadTremAmarelo;
  private ThreadTremAzul threadTremAzul;

  private boolean reiniciou = true;// variavel auxiliar par controle de acoes ao reiniciar a simulacao

  private int solucaoEscolhida = 1;// solucao para colisao aplicada a simulacao, inicialmente VarTravamento
  private int posicaoDosTrensEscolhida = 1;// posicao de partida dos trens, inicialmente ambos na Esq

  // Variavel de travamento, inicialmente ambos os trilhos estao livres
  private volatile int variavelDeTravamento_RC_1 = 0;// 0 = RC vazia (pode passsar), 1 = RC ocupada (espere)
  private volatile int variavelDeTravamento_RC_2 = 0;// 0 = RC vazia (pode passsar), 1 = RC ocupada (espere)

  // Estrita alternancia, inicialmente eh a vez do trem amarelo executar
  private volatile int vez_RC_1 = 0;// 0 = vez do trem amarelo executar a RC, 1 = vez do trem azul executar a RC
  private volatile int vez_RC_2 = 0;// 0 = vez do trem amarelo executar a RC, 1 = vez do trem azul executar a RC

  // Solucao de peterson
  private static final int N = 2;// define tamanho do vetor de interesse de acordo ao numero de threads
  private volatile int vezPeterson_RC_1;// vez de executar a RC, 0 = TremAmarelo, 1 = TremAzul
  private volatile int vezPeterson_RC_2;// vez de executar a RC, 0 = TremAmarelo, 1 = TremAzul
  private volatile int[] interesse_RC_1 = new int[N];// 1 = processo interessado em executar a RC
 private volatile int[] interesse_RC_2 = new int[N];// 1 = processo interessado em executar a RC

  // METODOS VARIAVEL DE TRAVAMENTO
  public int podeEntrarNa_RC_1() {// verifica o estado da regiao critica 1
    return variavelDeTravamento_RC_1;// 0 = vazia, 1 = ocupada
  }// fim do metodo podeEntrarNa_RC_1

  public int podeEntrarNa_RC_2() {// verifica o estado da regiao critica 2
    return variavelDeTravamento_RC_2;// 0 = vazia, 1 = ocupada
  }// fim do metodo podeEntrarNa_RC_2

  public void ocupar_RC_1() {// sinaliza que a regiao critica 1 esta ocupada
    this.variavelDeTravamento_RC_1 = 1;
  }// fim do metodo ocupar_RC_1

  public void ocupar_RC_2() {// sinaliza que a regiao critica 2 esta ocupada
    this.variavelDeTravamento_RC_2 = 1;
  }// fim do metodo ocupar_RC_2

  public void deixar_RC_1() {// sinaliza que a regiao critica 1 esta vazia
    this.variavelDeTravamento_RC_1 = 0;
  }// fim do metodo deixarRC_1

  public void deixar_RC_2() {// sinaliza que a regiao critica 2 esta vazia
    this.variavelDeTravamento_RC_2 = 0;
  }// fim do metodo deixarRC_2

  // METODOS ESTRITA ALTERNACIA
  public int vezDeExecutar_RC_1() {// retorna de quem eh a vez de andar na regiao critica 1
    return vez_RC_1;// 0 = vez do trem amarelo, 1 = vez do trem azul
  }// fim do metodo vezDeExecutar_RC_1

  public int vezDeExecutar_RC_2() {// retorna de quem eh a vez de andar na regiao critica 2
    return vez_RC_2;// 0 = vez do trem amarelo, 1 = vez do trem azul
  }// fim do metodo vezDeExecutar_RC_2

  public synchronized void sinalizarVezDoProximoExecutar_RC_1() {
    vez_RC_1 = (vez_RC_1 + 1) % 2;// inverte o valor para indicar a vez do proximo
  }// fim do metodo sinalizarVezDoProximoExecutar_RC_1

  public synchronized void sinalizarVezDoProximoExecutar_RC_2() {
    vez_RC_2 = (vez_RC_2 + 1) % 2;// inverte o valor para indicar a vez do proximo
  }// fim do metodo sinalizarVezDoProximoExecutar_RC_2

  // METODOS SOLUCAO DE PETERSON
  public void ENTRAR_RC_1(int processo) {
    int outro = 1 - processo;// num oposto ao id do processo
    interesse_RC_1[processo] = 1;// indica que o atual processo tem iteresse de entra na RC
    vezPeterson_RC_1 = processo;// indica que eh a vez do processo atual executar a RC

    if ((vezPeterson_RC_1 == processo) && (interesse_RC_1[outro] != 1)) {// vez do processo atual e o outro nao estiver na RC
      // EXECUTA A RC
    } // fim do if

    while ((vezPeterson_RC_1 == processo) && (interesse_RC_1[outro] == 1)) {// vez do processo atual e outro esta na RC
      // ESPERA ATE O OUTRO PROCESSO EXECUTAR A RC
    } // fim do while
  }// fim do metodo ENTRAR_RC

  public void DEIXAR_RC_1(int processo) {
    interesse_RC_1[processo] = 0;// atualiza o vetor para indicar que o processo atual deixou a RC
  }// fim do metodo DEIXAR_RC

  public void ENTRAR_RC_2(int processo) {
    int outro = 1 - processo;// num oposto ao id do processo
    interesse_RC_2[processo] = 1;// indica que o atual processo tem iteresse de entra na RC
    vezPeterson_RC_2 = processo;// indica que eh a vez do processo atual executar a RC

    if ((vezPeterson_RC_2 == processo) && (interesse_RC_2[outro] != 1)) {// vez do processo atual e o outro nao estiver na RC
      // EXECUTA A RC
    } // fim do if

    while ((vezPeterson_RC_2 == processo) && (interesse_RC_2[outro] == 1)) {// vez do processo atual e outro esta na RC
      // ESPERA ATE O OUTRO PROCESSO EXECUTAR A RC
    } // fim do while
  }// fim do metodo ENTRAR_RC

  public void DEIXAR_RC_2(int processo) {
    interesse_RC_2[processo] = 0;// atualiza o vetor para indicar que o processo atual deixou a RC
  }// fim do metodo DEIXAR_RC

  // Metodos para controlar o estado das threads
  public void criarThreadsDosTrens() {// cria os objetos
    threadTremAmarelo = new ThreadTremAmarelo(this, imvTremAmarelo, imgEsqTrmAmarelo, imgDirTrmAmarelo);
    threadTremAzul = new ThreadTremAzul(this, imvTremAzul, imgEsqTrmAzul, imgDirTrmAzul);
  }// fim do metodo criarThreadsDosTrens

  public void iniciarThreadsDosTrens() {
    threadTremAmarelo.start();// executa o metodo run() da thread
    threadTremAzul.start();// executa o metodo run() da thread
  }// fim do metodo iniciarThreadsDosTrens

  // reinicia as variaveis para evitar possiveis erros ao reiniciar a simulacao
  public void reiniciarVariaveisDasSolucoes() {
    // Variavel de travamento
    variavelDeTravamento_RC_1 = 0;
    variavelDeTravamento_RC_2 = 0;
    // Estrita alternancia
    vez_RC_1 = 0;
    vez_RC_2 = 0;
    // Solucao de Peterson
    for (int i = 0; i < N; i++) {// zera o vetor de interesse
      interesse_RC_1[i] = 0;
      interesse_RC_2[i] = 0;
    } // fim do for
  }// fim do metodo reiniciarVariaveisDasSolucoes

  /* ***************************************************************
   * Metodo: initialize
   * Funcao: Definir acoes que serao tomadas a inicar a interface
   * Parametros: void
   * Retorno: void.
   *************************************************************** */
  @Override
  public void initialize(URL arg0, ResourceBundle agr1) {

    // ao iniciar
    desabilitarBtnReiniciar();// oculta o botao reiniciar
    desabilitarSliders();// desabilita a interacao com os controles de velocidade
    atualizarPainelDeInfoColisao();// exibe a solucao para a colisao predefinida
    criarThreadsDosTrens();

    // metodo "ouvinte" para a interacoes com o controle de velocidade do trem
    // amarelo
    ctrlVelocidadeAmarelo.valueProperty().addListener((observable, valorAntigo, novoValor) -> {

      if (novoValor.intValue() > valorAntigo.intValue()) {// aumentou a velocidade
        moverSliderAmareloParaDireita(novoValor.intValue());// aumenta a velocidade
      } else {// diminui a velocidade
        moverSliderAmareloParaEsquerda(novoValor.intValue());// reduz ou para
      } // fim do if-else
    });// fim do metodo "ouvinte" trem amarelo

    // metodo "ouvinte" para a interacoes com o controle de velocidade do trem azul
    ctrlVelocidadeAzul.valueProperty().addListener((observable, valorAntigo, novoValor) -> {

      if (novoValor.intValue() > valorAntigo.intValue()) {// aumentou a velocidade
        moverSliderAzulParaDireita(novoValor.intValue());// aumenta a velocidade
      } else {// diminui a velocidade
        moverSliderAzulParaEsquerda(novoValor.intValue());// reduz ou para
      } // fim do if-else

    });// fim do metodo "ouvinte" trem azul
  }// fim do metodo initialize

  // Metodos para interagir com botoes ou demais elementos da tela
  public void acaoCliqueBtnEsqEsq() {// acoes tomadas ao clicar no botao (>>)
    setPosicaoDosTrensEscolhida(1);// sinaliza a nova posicao para as threads
    if (reiniciou) {// se a simulacao foi reiniciada
      threadTremAmarelo.mudarParaLadoEsquerdoDaTela();// muda a imagem de posicao
      threadTremAzul.mudarParaLadoEsquerdoDaTela();// muda a imagem de posicao
    } // fim do if
  }// fim do metodo acaoCliqueBtnEsqEsq

  public void acaoCliqueBtnEsqDir() {// acoes tomadas ao clicar no botao (><)
    setPosicaoDosTrensEscolhida(2);// sinaliza a nova posicao para as threads
    if (reiniciou) {// se a simulacao foi reiniciada
      threadTremAmarelo.mudarParaLadoEsquerdoDaTela();// muda a imagem de posicao
      threadTremAzul.mudarParaLadoDireitoDaTela();// muda a imagem de posicao
    } // fim do if
  }// fim do metodo acaoBtnEsqDir

  public void acaoCliqueBtnDirEsq() {// acoes tomadas ao clicar no botao (<>)
    setPosicaoDosTrensEscolhida(3);// sinaliza a nova posicao para as threads
    if (reiniciou) {// se a simulacao foi reiniciada
      threadTremAmarelo.mudarParaLadoDireitoDaTela();// muda a imagem de posicao
      threadTremAzul.mudarParaLadoEsquerdoDaTela();// muda a imagem de posicao
    } // fim do if
  }// fim do metodo acaoCliqueBtnDirEsq

  public void acaoCliqueBtnDirDir() {// acoes tomadas ao clicar no botao (<<)
    setPosicaoDosTrensEscolhida(4);// sinaliza a nova posicao para as threads
    if (reiniciou) {// se a simulacao foi reiniciada
      threadTremAmarelo.mudarParaLadoDireitoDaTela();// muda a imagem de posicao
      threadTremAzul.mudarParaLadoDireitoDaTela();// muda a imagem de posicao
    } // fim do if
  }// fim do metodo acaoCliqueBtnDirDir

  public void acaoCliqueBtnReiniciar() {// acoes tomadas ao clicar no botao "Reinciar Simulacao"
    setPosicaoDosTrensEscolhida(1);// ambos

    reiniciou = true;// sinaliza que reiniciou

    threadTremAmarelo.reiniciar();// interrompe a thread
    threadTremAzul.reiniciar();// interrompe a thread
    // mudancas feitas na interface grafica
    Platform.runLater(() -> desabilitarSliders());
    Platform.runLater(() -> habilitarMenuDeSolucoes());
    Platform.runLater(() -> habilitarBtnIniciar());
    Platform.runLater(() -> desabilitarBtnReiniciar());
  }// fim do metodo acaoCliqueBtnReiniciar

  public void acaoCliqueBtnIniciar() {// acoes tomadas ao clicar no botao "Iniciar Simulacao"
    // mudancas feitas na interface grafica
    Platform.runLater(() -> habilitarSliders());
    Platform.runLater(() -> desabilitarMenuDeSolucoes());
    Platform.runLater(() -> habilitarBtnReiniciar());
    Platform.runLater(() -> desabilitarBtnIniciar());

    exibirTelaDeInstrucoes();// exibe a janela com as intrucoes sobre a simulacao

    if (reiniciou) {// se reiniciou e clicou no botao "Iniciar Simulacao" novamente
      try {
        // cria e inicia as threads novamente
        criarThreadsDosTrens();
        reiniciarVariaveisDasSolucoes();// redefine os valores das variaveis
        iniciarThreadsDosTrens();
      } catch (Exception e) {
        // captura a excecao gera ao chamar o metodo run() mais de uma vez
      }
      reiniciou = false;// atualiza o estado
    } // fim do if
  }// fim do metodo acaoCliqueBtnIniciar

  public void acaoCliqueBtnVarDeTravamento() {// acoes tomadas ao selecionar a Variavel de Travamento
    setSolucaoEscolhida(1);// informa as threads a solucao escolhida
    atualizarPainelDeInfoColisao();// exibe a solucao aplicada a simulacao
  }// fim do metodo acaoCliqueBtnVarDeTravamento

  public void acaoCliqueBtnEstritaAlternacia() {// acoes tomadas ao selecionar a Estrita Alternancia
    setSolucaoEscolhida(2);// informa as threads a solucao escolhida
    atualizarPainelDeInfoColisao();// exibe a solucao aplicada a simulacao
  }// fim do metodo acaoCliqueBtnEstritaAlternacia

  public void acaoCliqueBtnSolDePeterson() {// acoes tomadas ao selecionar a Solucao de Peterson
    setSolucaoEscolhida(3);// informa as threads a solucao escolhida
    atualizarPainelDeInfoColisao();// exibe a solucao aplicada a simulacao
  }// fim do metodo acaoCliqueBtnSolDePeterson

  public void moverSliderAmareloParaDireita(int valorObservado) {// acoes tomadas ao mover o slider para direita
    if (valorObservado > 0) {// aumentou a velocidade
      // exibe a velocidade atual
      Platform.runLater(() -> painelInfoAmarelo.setText("Se movendo a " + (valorObservado) + " km/h."));
      threadTremAmarelo.aumentarVelocidade();// aumenta a velocidade do trem
    } else {// o trem parou
      Platform.runLater(() -> painelInfoAmarelo.setText("Parado."));// informa que o trem parou
      threadTremAmarelo.parar();// para o trem
    } // fim do if-else
  }// fim do metodo moverSliderAmareloParaDireita

  public void moverSliderAmareloParaEsquerda(int valorObservado) {// acoes tomadas ao mover o slider para direita
    if (valorObservado > 0) {// diminuiu a velocidade
      // exibe a velocidade atual
      Platform.runLater(() -> painelInfoAmarelo.setText("Se movendo a " + (valorObservado) + " km/h."));
      threadTremAmarelo.diminuirVelocidade();// dimuit a velocidade do trem
    } else {// o trem parou
      Platform.runLater(() -> painelInfoAmarelo.setText("Parado."));// informa que o trem parou
      threadTremAmarelo.parar();// para o trem
    } // fim do if-else
  }// fim do metodo moverSliderAmareloParaEsquerda

  public void moverSliderAzulParaDireita(int valorObservado) {// acoes tomadas ao mover o slider para direita
    if (valorObservado > 0) {// aumentou a velocidade
      // exibe a velocidade atual
      Platform.runLater(() -> painelInfoAzul.setText("Se movendo a " + (valorObservado) + " km/h."));
      threadTremAzul.aumentarVelocidade();// aumenta a velocidade do trem
    } else {// o trem parou
      Platform.runLater(() -> painelInfoAzul.setText("Parado."));// informa que o trem parou
      threadTremAzul.parar();// para o trem
    } // fim do if-else
  }// fim do metodomoverSliderAzulParaDireita

  public void moverSliderAzulParaEsquerda(int valorObservado) {// acoes tomadas ao mover o slider para esquerda
    if (valorObservado > 0) {// diminuiu a velocidade
      // exibe a velocidade atual
      Platform.runLater(() -> painelInfoAzul.setText("Se movendo a " + (valorObservado) + " km/h."));
      threadTremAzul.diminuirVelocidade();// diminui a velocidade do trem
    } else {
      Platform.runLater(() -> painelInfoAzul.setText("Parado."));// informa que o trem parou
      threadTremAzul.parar();// para o trem
    } // fim do if-else
  }// fim do metodo moverSliderAzulParaEsquerda

  // Metodos para exibir, atualizar, habilitar ou desabilitar itens e informacoes
  public void atualizarPainelDeInfoColisao() {// informa qual solucao para a colisao sera aplicada na simulacao
    switch (getSolucaoEscolhida()) {// analiza o valor que indica a solucao escolhida
      case (1): {// escolheu variavel de travamento
        painelInfoColisao.setText("Solução 1: Variável de travamento");
        break;
      } // fim do case 1
      case (2): {// escolheu estrita alternancia
        painelInfoColisao.setText("Solução 2: Estrita alternância");
        break;
      } // fim do case 2
      case (3): {// escolheu solucao de peterson
        painelInfoColisao.setText("Solução 3: Solução de Peterson");
      } // fim do case 3
    }// fim do switch
  }// fim do metodo atualizarPainelDeInfoColisao

  public void exibirTelaDeInstrucoes() {// exibe uma janela com algumas instrucoes ao iniciar a simulacao
    Alert aviso = new Alert(AlertType.INFORMATION);

    VBox caixaDeDialogo = new VBox();
    caixaDeDialogo.setPrefWidth(500);
    caixaDeDialogo.setPrefHeight(150);
    caixaDeDialogo.getChildren().add(new Label(
     "1. Clique no controle de velocidade de um trem para selecioná-lo."
            + "\nEm seguida, utilize as teclas direcionais Direita e Esquerda do seu "
            + "\nteclado para aumentar ou diminuir a velocidade, respectivamente."
            + "\n2. Para alterar a solução de colisão aplicada, clique no botão 'Reiniciar'"
            + "\nselecione a solução desejada e inicie uma nova simulação."
            + "\n3. A posição de partida durante a simulação só é alterada após o trem "
            + "\ncompletar o seu percurso atual."));
    aviso.getDialogPane().setContent(caixaDeDialogo);
    aviso.setTitle("Atenção!");
    aviso.setHeaderText("Instruções para controlar a simulação");

    aviso.showAndWait();// espera a janela ser fechada para movimentar os trens
  }// fim do metodo exibirTelaDeInstrucoes

  public void habilitarMenuDeSolucoes() {// habilita o menu solucoes para interacoes
    menuDeSolucoes.setDisable(false);
    btnVarDeTravamento.setDisable(false);
    btnEstritaAlternacia.setDisable(false);
    btnSolDePeterson.setDisable(false);
  }// fim do metodo habilitarMenuDeSolucoes

  public void desabilitarMenuDeSolucoes() {// desabilita o menu de solucoes para interacoes
    menuDeSolucoes.setDisable(true);
    btnVarDeTravamento.setDisable(true);
    btnEstritaAlternacia.setDisable(true);
    btnSolDePeterson.setDisable(true);
  }// fim do metodo desabilitarMenuDeSolucoes

  public void habilitarBtnReiniciar() {// habilita e exibe o botao reiniciar para interacoes
    btnReiniciar.setDisable(false);
    btnReiniciar.setOpacity(1);
  }// fim do metodo habilitarBtnReiniciar

  public void desabilitarBtnReiniciar() {// desabilita e oculta o botao reiniciar para interacoes
    btnReiniciar.setDisable(true);
    btnReiniciar.setOpacity(0);
  }// fim do metodo desabilitarBtnReiniciar

  public void habilitarBtnIniciar() {// habilita e exibe o botao iniciar para interacoes
    btnIniciar.setDisable(false);
    btnIniciar.setOpacity(1);
  }// fim do metodo habilitarBtnIniciar

  public void desabilitarBtnIniciar() {// desabilita e oculta o botao iniciar para interacoes
    btnIniciar.setDisable(true);
    btnIniciar.setOpacity(0);
  }// fim do metodo desabilitarBtnIniciar

  public void habilitarSliders() {// habilita os controles de velocidade para interacoes e define um valor inicial
    ctrlVelocidadeAmarelo.setDisable(false);
    ctrlVelocidadeAzul.setDisable(false);
    ctrlVelocidadeAmarelo.setValue(8);// define um valor inicial
    ctrlVelocidadeAzul.setValue(8);// define um valor inicial
    iniciarPaineisDeVelocidade();// informa o valor definido
  }// fim do metodo habilitarSliders

  public void desabilitarSliders() {// desabilita os controles de velocidade para interacoes e zera
    ctrlVelocidadeAmarelo.setDisable(true);
    ctrlVelocidadeAzul.setDisable(true);
    ctrlVelocidadeAmarelo.setValue(0);// zera o controle
    ctrlVelocidadeAzul.setValue(0);// zera o controle
    iniciarPaineisDeVelocidade();// exibe a mudanca
  }// fim do metodo desabilitarSliders

  public void iniciarPaineisDeVelocidade() {// exibe as valores de velocidade corretos ao iniciar e reiniciar
    int velocidadeAmarelo = (int) ctrlVelocidadeAmarelo.getValue();// converte e armazena o valor atual do slider
    int velocidadeAzul = (int) ctrlVelocidadeAzul.getValue();// converte e armazena o valor atual do slider

    // trem amarelo
    if (velocidadeAmarelo > 0) {
      Platform.runLater(() -> painelInfoAmarelo.setText("Se movendo a " + velocidadeAmarelo + " km/h."));
    } else {
      Platform.runLater(() -> painelInfoAmarelo.setText("Parado."));
    } // fim do bloco if-else

    // trem azul
    if (velocidadeAzul > 0) {
      Platform.runLater(() -> painelInfoAzul.setText("Se movendo a " + velocidadeAzul + " km/h."));
    } else {
      Platform.runLater(() -> painelInfoAzul.setText("Parado."));
    } // fim do bloco if-else
  }// fim do metodo atualizarPaineisDeVelocidade

  // Getters e setters
  public void setSolucaoEscolhida(int solucaoEscolhida) {
    this.solucaoEscolhida = solucaoEscolhida;
  }// fim do metodo setSolucaoEscolhida
  public void setPosicaoDosTrensEscolhida(int posicaoDosTrensEscolhida) {
    this.posicaoDosTrensEscolhida = posicaoDosTrensEscolhida;
  }// fim do metodo setPosicaoDosTrensEscolhida

  public int getPosicaoDosTrensEscolhida() {
    return posicaoDosTrensEscolhida;
  }// fim do metodo setPosicaoDosTrensEscolhida

  public int getSolucaoEscolhida() {
    return solucaoEscolhida;
  }// fim do metodo getSolucaoEscolhida

}// fim da classe TelaPrincipal
