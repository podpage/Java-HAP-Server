# Java-HAP-Server
HAP-Server based on [beowulfe/HAP-Java](https://github.com/beowulfe/HAP-Java)

This project would not have been possible without the work of [Tian Zhang](https://github.com/KhaosT) and [Andy Lintner](https://github.com/beowulfe)! <3

### Installation

  - 1). Download
  - 2). Start and Stop (generating config and folders)
  - 3). Dump accessory files in "plugins" folder
  - 4). Start
  - 5). Connect your iPhone
  - 6). done!


### Plugins

```java
@LoadableAccessory
public class MockLight implements Lightbulb {
```

Create an accessory and add the @LoadableAccessory annotation on top of the class.
Export as .jar and put the file in the "plugins" folder.
Easy as that


If you need help or have any question just text me [@podpage](https://twitter.com/podpage)
