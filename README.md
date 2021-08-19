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

		La salida cambia porque al cambiar de método (es decir, *run()* en vez de *start()*), ya no se está inicializando el hilo, esto es porque el método *start()* 			permite que el hilo se pueda iniciar ya que este es un método de la clase *Thread*, así que cuando se realiza la ejecución con *run()* se hace la ejecución  	secuencialmente y en orden en ves de paralelamente por hilos.

## **PARTE II**
1. Cree una clase de tipo Thread que represente el ciclo de vida de un hilo que haga la búsqueda de un segmento del conjunto de servidores disponibles. Agregue a dicha clase un método que permita 'preguntarle' a las instancias del mismo (los hilos) cuantas ocurrencias de servidores maliciosos ha encontrado o encontró.
    ```JAVA
        public class CheckSegmentHost implements Runnable{
	
        	public final Thread ownThread;
            
            private int start;
            private int finish;
            private boolean hasBeenFinalized;
            private String ipAddress;
            private HostBlacklistsDataSourceFacade skds;
            private ArrayList<Integer> blackListOcurrences;
            
            public CheckSegmentHost(int start, int finish, String ipAddress) {
                ownThread = new Thread(this);
                this.start = start;
                this.finish = finish;
                this.ipAddress = ipAddress;
                hasBeenFinalized = false;
                blackListOcurrences = new ArrayList<>();
                skds = HostBlacklistsDataSourceFacade.getInstance();
            }
        
            @Override
            public void run() {
                for (int i = start; i <= finish; i++){
                    if (skds.isInBlackListServer(i, ipAddress)){
                        blackListOcurrences.add(i);
                    }
                }
                synchronized (this) {
                	hasBeenFinalized = true;
                	notify();
                }
            }
        
            public synchronized int getOcurrencesCount() {
            	try {
            		while (!hasBeenFinalized) {
            			wait();
            		}
            	} catch (InterruptedException e) {
            		System.out.println(ownThread.getName()+" Interrupted...");
            	}
                return blackListOcurrences.size();
            }
        
            public ArrayList<Integer> getBlackListOcurrences(){
            	try {
            		while (!hasBeenFinalized) {
            			wait();
            		}
            	} catch (InterruptedException e) {
            		System.out.println(ownThread.getName()+" Interrupted...");
            	}
            	return blackListOcurrences;
            }
        }
    ```

2. Agregue al método 'checkHost' un parámetro entero N, correspondiente al número de hilos entre los que se va a realizar la búsqueda (recuerde tener en cuenta si N es par o impar!). Modifique el código de este método para que divida el espacio de búsqueda entre las N partes indicadas, y paralelice la búsqueda a través de N hilos. Haga que dicha función espere hasta que los N hilos terminen de resolver su respectivo sub-problema, agregue las ocurrencias encontradas por cada hilo a la lista que retorna el método, y entonces calcule (sumando el total de ocurrencuas encontradas por cada hilo) si el número de ocurrencias es mayor o igual a BLACK_LIST_ALARM_COUNT. Si se da este caso, al final se DEBE reportar el host como confiable o no confiable, y mostrar el listado con los números de las listas negras respectivas.

    **(Fragmento que controla la distribución de hilos según si es par o impar)**
    ```JAVA
        if (remainingServers == 0) {
        	for (int i = 0; i < N; i++) {
        		threads.add(new CheckSegmentHost((i*serversPerThread)+1, (i+1)*serversPerThread, ipaddress));
        	}
        }
        else {
        	int starterSegment;
        	int lastSegment = 0;
        	for (int i = 0; i < remainingServers; i++) {
        		starterSegment = lastSegment + 1;
        		lastSegment = starterSegment + serversPerThread;
        		threads.add(new CheckSegmentHost(starterSegment, lastSegment, ipaddress));
        	}
        	starterSegment = remainingServers*(serversPerThread+1);
        	lastSegment = starterSegment + (serversPerThread-1);
        	for (int j = remainingServers; j < N; j++) {
        		threads.add(new CheckSegmentHost(starterSegment, lastSegment, ipaddress));
        		starterSegment = lastSegment + 1;
        		lastSegment = starterSegment + (serversPerThread-1);
        	}
        }
    ```
    
    **(Este fragmento permite que los hilos puedan iniciarse y terminar de manera simultanea)**
    ```JAVA
        for (CheckSegmentHost t : threads) {
            t.ownThread.start();
            System.out.println("Ejecutando Thread: " + t.ownThread.getName());
        }

        for (CheckSegmentHost t : threads) {
        	try {
            	t.ownThread.join();
            } catch (Exception e) {
        	    e.printStackTrace();
            }
            System.out.println("Terminó Thread: " + t.ownThread.getName());
        }
    ```
    
    **(Este fragmento permite agregar las ocurrencias de todos los hilos a una sola lista. También realiza la clasificación del host como *confiable* o *no confiable*)**
    ```JAVA
       for (CheckSegmentHost t : threads) {
            ocurrencesCount += t.getOcurrencesCount();
            blackListOcurrences.addAll(t.getBlackListOcurrences());
        }
        
        if (ocurrencesCount >= BLACK_LIST_ALARM_COUNT){
            skds.reportAsNotTrustworthy(ipaddress);
        }
        else{
            skds.reportAsTrustworthy(ipaddress);
        } 
    ```
    
   Se sabe que el HOST 202.24.34.55 está reportado en listas negras de una forma más dispersa, y que el host 212.24.24.55 NO está en ninguna lista negra.
   
   **(HOST 202.24.34.55)**
    ```
       The host was found in the following blacklists: [29, 10034, 20200, 31000, 70500] 
    ```
   **(HOST 212.24.24.55)**
    ```
        The host was found in the following blacklists: []
    ```

## **PARTE III**

1. **VALORES DE CADA EJECUCIÓN**

    (Los valores fueron obtenidos evaluando las ejecuciones del Host 202.24.34.55)
    - 1 Hilo
        ![Ejecución con 1 Hilo](https://github.com/Brayandres/ARSW-LAB-01/blob/master/img/Monitor1T.JPG?raw=true)
    - 4 Hilos (valor obtenido del Runtime, método *availableProcessors()*)
        ![Ejecución con 1 Hilo](https://github.com/Brayandres/ARSW-LAB-01/blob/master/img/Monitor4T.JPG?raw=true)
    - 8 Hilos (2 veces *availableProcessors()*)
        ![Ejecución con 1 Hilo](https://github.com/Brayandres/ARSW-LAB-01/blob/master/img/Monitor8T.JPG?raw=true)
    - 50 Hilos
        ![Ejecución con 1 Hilo](https://github.com/Brayandres/ARSW-LAB-01/blob/master/img/Monitor50T.JPG?raw=true)
    - 100 Hilos
        ![Ejecución con 1 Hilo](https://github.com/Brayandres/ARSW-LAB-01/blob/master/img/Monitor100T.JPG?raw=true)

2. **GRÁFICOS DE TIEMPO VS HILOS**

    (El tiempo es el de ejecución y está en Segundos, con 3 decimales de exactitud)

    - Host 200.24.34.55
        | # HILOS | TIEMPO |
        |---------|--------|
        |    1    |159.142 |
        |    4    |41.954  |
        |    8    |21.793  |
        |   50    |3.669   |
        |   100   |1.932   |
	
        ![Gráfico 1](https://github.com/Brayandres/ARSW-LAB-01/blob/master/img/Gr%C3%A1fico1.jpg?raw=true)
    
    - Host 202.24.34.55
        | # HILOS | TIEMPO |
        |---------|--------|
        |    1    |160.186 |
        |    4    |41.864  |
        |    8    |21.898  |
        |   50    |3.591   |
        |   100   |1.876   |
	
        ![Gráfico 2](https://github.com/Brayandres/ARSW-LAB-01/blob/master/img/Gr%C3%A1fico2.jpg?raw=true)
        
    - Host 212.24.24.55
        | # HILOS | TIEMPO |
        |---------|--------|
        |    1    |159.907 |
        |    4    |42.111  |
        |    8    |22.053  |
        |   50    |3.683   |
        |   100   |1.899   |
	
        ![Gráfico 3](https://github.com/Brayandres/ARSW-LAB-01/blob/master/img/Gr%C3%A1fico3.jpg?raw=true)

