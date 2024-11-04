import java.util.concurrent.*;

class ComplexTask {
    private final int id;

    public ComplexTask(int id) {
        this.id = id;
    }

    public int getId(){
        return id;
    }

    public void execute() {
        System.out.println("Task "+id+" is executed "+Thread.currentThread().getName());
        try {
            Thread.sleep((long) (Math.random() * 1000));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}

class TaskWorker implements Runnable {
    private ComplexTask task;
    private CyclicBarrier barr;

    public TaskWorker(ComplexTask task, CyclicBarrier barr) {
        this.task = task;
        this.barr = barr;
    }

    @Override
    public void run() {
        task.execute();
        try {
            System.out.println("Task "+task.getId()+" done by "+Thread.currentThread().getName());
            barr.await();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}

class ComplexTaskExecutor {
    private int tasks;
    private CyclicBarrier barr;
    private ExecutorService exec;

    public ComplexTaskExecutor(int tasks) {
        this.tasks = tasks;
        this.barr = new CyclicBarrier(tasks, this::combineResults);
        this.exec = Executors.newFixedThreadPool(tasks);
    }

    public void executeTasks(int numberOfTasks) {
        for (int i=0; i<numberOfTasks; i++) {
            ComplexTask task = new ComplexTask(i+1);
            TaskWorker worker = new TaskWorker(task, barr);
            exec.submit(worker);
        }
    }

    public void shutdownExecutor() {
        exec.shutdown();
        try {
            if (!exec.awaitTermination(1, TimeUnit.MINUTES)) {
                System.out.println("Forced shutdown");
                exec.shutdownNow();
            }
        } catch (Exception e) {
            exec.shutdownNow();
            System.out.println(e.getMessage());
        }
    }

    private void combineResults() {
        System.out.println("All tasks reached the barrier");
    }
}

public class TestComplexTaskExecutor {

    public static void main(String[] args) {
        ComplexTaskExecutor taskExecutor = new ComplexTaskExecutor(5); // Количество задач для выполнения

        Runnable testRunnable = () -> {
            System.out.println(Thread.currentThread().getName() + " started the test.");

            // Выполнение задач
            taskExecutor.executeTasks(5);

            System.out.println(Thread.currentThread().getName() + " completed the test.");
        };

        Thread thread1 = new Thread(testRunnable, "TestThread-1");
        Thread thread2 = new Thread(testRunnable, "TestThread-2");

        thread1.start();
        thread2.start();

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

