# **Escuela Colombiana de Ingeniería**
# **LABORATORIO #1 – ARSW**
## Integrantes
- Garzón Kevin
- Macías Brayan

## Repositorio Fuente
- [Ejercicio Introducción al paralelismo - Hilos - Caso BlackListSearch](https://github.com/ARSW-ECI-beta/PARALLELISM-JAVA_THREADS-INTRODUCTION_BLACKLISTSEARCH.git)

## **PARTE I**
1. De acuerdo con lo revisado en las lecturas, complete las clases CountThread, para que las mismas definan el ciclo de vida de un hilo que imprima por pantalla los números entre A y B.
    ```JAVA
        public class CountThread implements Runnable {
    
        private int a;
        private int b;
    
        @Override
        public void run() {
            for (int i = a; i <= b; i++){
                System.out.println(i);
            }
        }
    
        public CountThread(int a, int b) {
            this.a = a;
            this.b = b;
        }
    }
    ```

2. Complete el método main de la clase CountMainThreads para que:
   +	Cree 3 hilos de tipo CountThread, asignándole al primero el intervalo [0..99], al segundo [99..199], y al tercero [200..299]. Inicie los tres hilos con 'start()'.
        ```java
           public class CountThreadsMain {
        
            public static void main(String a[]){
                Thread t1 = new Thread(new CountThread(0, 99));
                Thread t2 = new Thread(new CountThread(100, 199));
                Thread t3 = new Thread(new CountThread(200, 299));
        
                t1.start();
                t2.start();
                t3.start();
            }
        }
        ```

   +	Ejecute y revise la salida por pantalla.
        Cambie el inicio con 'start()' por 'run()'.
        
        ![start() vs. run()](https://raw.githubusercontent.com/Brayandres/ARSW-LAB-01/master/img/Image3.jpg)

   +    ¿Cómo cambia la salida? ¿por qué?

## **PARTE II**
