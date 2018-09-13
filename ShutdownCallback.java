public class ShutdownCallback {
    private static List<Closeable> closes = new ArrayList<>();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                for (Closeable closeable :
                        closes) {
                    try {
                        closeable.close();
                        log.info("finish close ->{}",closeable.toString());
                    } catch (IOException e) {
                        log.error(e.toString());
                        log.error(closeable.toString());
                    }
                }
            }
        });
    }

    public static void register(Closeable closeable) {
        closes.add(closeable);
    }
}
