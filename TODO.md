TODO
=====

####Regras:

 - Quando terminar um item passe para o próximo item

 - Se não quiser fazer um determinado item passe-o para outra pessoa e **avise**

 - Termine o quanto antes, e lembre que ainda tem os testes/correções, documentação, material escrito e o curso!
 
 - **Não tenha medo de fazer críticas e propor mudanças**


####Grupo 1: Luís, Diego e Fernando

 - [ ] Implementar Dispositivos (Java e C++)

  - ver: **Device**->Compass->HBridge->*Robot*

 - [ ] Implementar Comandos e funções de alto nível (Java e  talvez C++)

  - ver: Command->**Procedure**->Wait->PrintString->Move(refazer)->**ReadDevice**->*Interpreter*

 - [ ] **Se juntar ao grupo 2**

####Grupo 2: Ricardo e Rafael

 - [ ] Algoritmo de posicionamento dos componentes (Drawable) dentro de um Fluxograma (Rafael)

  - ver: **Command**->**Procedure**->**Block**->Function->If->While->*Interpreter.main()*->**Drawable**->**GraphicResource**
  
  - implementar em `Function` a função recursiva:

     ```java
     public static void indent (Function f); 
     ```
     
  - lembrando que em breve as classes `Command` e `Procedure` implementarão `GraphicResource` então é só usar:
     ```java
     Command c;
     if (c instanceof GraphicResource){
          Drawable d = ((GraphicResource)c).getDrawableResource();
          d.setObjectLocation(x,y);
          ...
     }
     
     ```

 - [ ] Painel de seleção de comandos para o fluxograma (Ricardo)
 
  - ver: **Drawable**->**DWidgetContainer**->**DrawingPanel**->GraphicResource->**ReadDevice**->SimulationPanel->*GUI*
  - implementar a classe `FlowCharPanel`:
  
     ```java
     public class FlowCharPanel extends DrawingPanel {...}
     ```

 - [ ] Drag and Drop

 - [ ] Save/Load

 - [ ] *Copy/Paste*

 - [ ] *Redo/Undo*

 - [ ] **Se juntar ao grupo 3**

####Grupo 3: Anderson e William

 - [x] Differential wheeled robot simulation (Anderson)

 - [ ] Painel de Simulação (Anderson)

 - [ ] Desenhar Dispositivos e Conexões
 
  - ver: **Drawable**->**DWidgetContainer**->*DrawingPanel*->GraphicResource->**ReadDevice**
  - editar todos os Dispositivos/Conexões/Comandos para implementar `GraphicResource`:
  
     ```java
     public class MyDevice extends Device implements GraphicResource {
         public Drawable getDrawableResource(){
             //veja ReadDevice
             return minhaClasseAnonimaQueImplementaDWidgetContainerOuDrawableETambemEhMembroDeMyDevice;
         }
     }
     ```
  

 - [ ] Desenhar Comandos

  - ver: **Drawable**->**DWidgetContainer**->*DrawingPanel*->GraphicResource->**ReadDevice**

 - [ ] Interface do Usuário final (GUI)
 
  - ver: **DrawingPanel**->**TabController**

##Arrumar/Implementar

- [ ] Passar uma variável para um dispositivo/Comandos que leem variáveis

 - ver: **Procedure**->**Declaration**->**ReadDevice**->**Device**

- [ ] *Funções que usem diversos dispositivos (C++) e Comando do protocolo para isso*

##Objetivos extras

 - [ ] Conversor Fluxograma <=> C-Like

 - [ ] Painel de Programação

 - [ ] Interface de Edição do Robô
 
**Nota:** *ver = entender o suficiente*

Divirta-se

