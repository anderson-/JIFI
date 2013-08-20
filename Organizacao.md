_Nova Organização do Projeto_
================

pacote robotinterface: 

    ├── algorithm *classes para criação de algoritmos*
    │   ├── Command.java
    │   └── procedure *classes que usam variaveis*
    │       ├── Block.java
    │       ├── Declaration.java
    │       ├── Function.java
    │       ├── If.java
    │       ├── Procedure.java
    │       └── While.java
    ├── drawable
    │   ├── Drawable.java
    │   ├── DrawingPanel.java
    │   ├── DWidgetContainer.java
    │   ├── exemples
    │   │   └── DrawableTest.java
    │   ├── GraphicResource.java
    │   └── util
    │       └── QuickFrame.java
    ├── gui
    │   ├── GUI.form
    │   ├── GUI.java
    │   └── panels
    │       ├── CodeEditorPanel.java
    │       ├── FlowchartPanel.java (Ricardo) *painel de controle fluxograma*
    │       ├── SimulationPanel.java
    │       └── TabControler.java
    ├── interpreter
    │   ├── ExecutionException.java
    │   ├── Expression.java
    │   └── Interpreter.java

    ├── plugins
    │   └── cmdpack
    │       ├── begginer
    │       │   ├── Move.java
    │       │   ├── ReadDevice.java
    │       │   └── Wait.java
    │       ├── serial
    │       │   ├── Start.java
    │       │   └── Stop.java
    │       └── util
    │           └── PrintString.java
    ├── resources (...)
    ├── robot
    │   ├── connection
    │   │   ├── Connection.java
    │   │   └── Serial.java
    │   ├── device
    │   │   ├── Compass.java
    │   │   ├── Device.java
    │   │   ├── HBridge.java
    │   │   └── StandartIO.java
    │   └── Robot.java
    └── util
        ├── ByteCharset.java
        ├── observable
        │   ├── Observable.java
        │   └── Observer.java
        └── trafficsimulator
            ├── Clock.java
            ├── ColorChanger.java
           └── Timer.java
