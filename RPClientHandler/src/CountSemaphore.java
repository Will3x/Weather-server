class CountSemaphore {

    private int max;
    private int count;

    CountSemaphore(int Getal){
        this.max = Getal;
        this.count = Getal;
    }

    synchronized void acquire() throws InterruptedException{
        while(this.count <= 0)
            wait();
        this.count--;
        this.notify();
    }

    synchronized void release() throws InterruptedException {
        while (this.count >= max)
            wait();
        this.count++;

        this.notify();
    }
}
