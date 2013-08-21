_Organização do Projeto_
================

pacote `robotinterface`: 

    ├── <algorithm> *criação de algoritmos*
    │   ├── Command.java - comando genérico
    │   └── <procedure> *variaveis*
    │       ├── Block.java - bloco de comandos
    │       ├── Declaration.java - declaração de variaveis
    │       ├── Function.java - função
    │       ├── If.java - divisor de fluxo
    │       ├── Procedure.java - comando genérico com suporte à variavies
    │       └── While.java - laço de repetição simples
    ├── <drawable> *desenho genérico com/sem Swing*
    │   ├── Drawable.java - interface que torna uma classe desenhável por um /DrawingPanel/
    │   ├── DrawingPanel.java
    │   ├── DWidgetContainer.java
    │   ├── <exemples> *exemplos*
    │   │   └── DrawableTest.java
    │   ├── GraphicResource.java
    │   └── <util> *uteis*
    │       └── QuickFrame.java
    ├── <gui> *interface gráfica*
    │   ├── GUI.form
    │   ├── GUI.java
    │   └── <panels> *abas usadas na GUI*
    │       ├── CodeEditorPanel.java
    │       ├── FlowchartPanel.java
    │       ├── SimulationPanel.java
    │       └── TabControler.java
    ├── <interpreter> *interpretador*
    │   ├── ExecutionException.java
    │   ├── Expression.java
    │   └── Interpreter.java
    ├── <plugins> *componentes removiveis*
    │   └── <cmdpack> *comandos de alto nível*
    │       ├── <begginer> *iniciante*
    │       │   ├── Move.java
    │       │   ├── ReadDevice.java
    │       │   └── Wait.java
    │       ├── <serial> *serial*
    │       │   ├── Start.java
    │       │   └── Stop.java
    │       └── <util> *uteis*
    │           └── PrintString.java
    ├── <resources> *imagens*
    ├── <robot> *robô*
    │   ├── <connection> *conexões*
    │   │   ├── Connection.java
    │   │   └── Serial.java
    │   ├── <device> *dispositivos*
    │   │   ├── Compass.java
    │   │   ├── Device.java
    │   │   ├── HBridge.java
    │   │   └── StandartIO.java
    │   └── Robot.java
    └── <util> *não tão uteis*
        ├── ByteCharset.java
        ├── <observable> *observer patern*
        │   ├── Observable.java
        │   └── Observer.java
        └── <trafficsimulator> *outras classes*
            ├── Clock.java
            ├── ColorChanger.java
            └── Timer.java
