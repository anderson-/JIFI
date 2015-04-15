_Organização do Projeto_
================

####[Clique aqui para saber o que tem para fazer.](https://github.com/anderson-/JIFI/blob/master/TODO.md)

Pacote `jifi`: 

    ├── <algorithm> *criação de algoritmos*
    │   ├── Command.java - comando genérico
    │   └── <procedure> *variaveis*
    │       ├── Block.java - bloco de comandos com suporte à escopo de variável
    │       ├── Declaration.java - declaração de variaveis
    │       ├── Function.java - função
    │       ├── If.java - divisor de fluxo
    │       ├── Procedure.java - comando genérico com suporte à variavies
    │       └── While.java - laço de repetição simples
    ├── <drawable> *desenho genérico com/sem Swing*
    │   ├── Drawable.java - interface que torna uma classe desenhável por um /DrawingPanel/
    │   ├── DrawingPanel.java - (extends JPanel implements Drawable) Painel para desenho de componentes desenháveis
    │   ├── DWidgetContainer.java - (antiga SwingContainer, implements Drawable) Container desenhável com suporte a componentes Swing
    │   ├── <exemples> *exemplos* 
    │   │   └── DrawableTest.java - exemplo de implementação de Drawable e DWidgetContainer
    │   ├── GraphicResource.java - interface para possibilitar classes (ex. Command/Procedure) retornar uma implementação (possivelmente anônima) de Drawable ou DWidgetContainer (veja: ReadDevice)
    │   └── <util> *uteis*
    │       └── QuickFrame.java - classe com membros estáticos para teste de componentes gráficos (veja: ReadDevice.main(...))
    ├── <gui> *interface gráfica*
    │   ├── GUI.form - coisa do netbeans 
    │   ├── GUI.java - janela principal do programa
    │   └── <panels> *abas usadas na GUI*
    │       ├── CodeEditorPanel.java - futura implementação do painel de programação por texto
    │       ├── FlowchartPanel.java - (Ricardo) painel que edita os fluxogramas
    │       ├── SimulationPanel.java - (Anderson) painel da simulação do robô
    │       └── TabControler.java - interface para possibilitar abas dinâmicas nos paineis laterais (propiedades, etc)
    ├── <interpreter> *interpretador*
    │   ├── ExecutionException.java - exceção lançada quando um comando não pode ser executado
    │   ├── Expression.java - classe interpretável (Command) com suporte à variáveis (usada por Procedure)
    │   └── Interpreter.java - responsável por interpretar os algoritmos e executar os comandos
    ├── <plugins> *componentes removiveis*
    │   └── <cmdpack> *comandos de alto nível*
    │       ├── <begginer> *iniciante*
    │       │   ├── Move.java - procedimento de mover o robô
    │       │   ├── ReadDevice.java - carrega o valor de um dispositivo em uma variável
    │       │   └── Wait.java - espera um tempo em ms
    │       ├── <serial> *serial*
    │       │   ├── Start.java - inicia a conexão serial
    │       │   └── Stop.java - encerra a conexão serial
    │       └── <util> *uteis*
    │           └── PrintString.java - exibe uma String na tela (tem suporte à exibição de variáveis)
    ├── <resources> *imagens*
    ├── <robot> *robô*
    │   ├── <connection> *conexões*
    │   │   ├── Connection.java - interface para implementação de uma conexão
    │   │   ├── Serial.java - conexão serial
    │   │   └── StandartIO.java - entrada/saida padrão (ainda não funcional)
    │   ├── <device> *dispositivos*
    │   │   ├── Compass.java - bussola
    │   │   ├── Device.java - classe abstrata para implementação de dispositivos
    │   │   └── HBridge.java - ponte h
    │   └── Robot.java - classe responsável pela agregação dos periféricos e interpretação do protocolo de comunicação
    └── <util> *não tão uteis*
        ├── ByteCharset.java - converção de byte [] -> String -> byte [] sem problemas
        ├── <observable> *observer patern*
        │   ├── Observable.java - objeto observável (Connection)
        │   └── Observer.java - objeto que observa (Robot)
        └── <trafficsimulator> *outras classes*
            ├── Clock.java - relógio global com suporte a timers
            ├── ColorChanger.java - altera uma cor dentro de um intervalo de cor e tempo
            └── Timer.java - timer com suporte à repetição e execução de funções
