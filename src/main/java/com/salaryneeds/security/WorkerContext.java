package com.salaryneeds.security;

public final class WorkerContext {
    private static final ThreadLocal<String> CURRENT_WORKER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_WORKER_PHONE = new ThreadLocal<>();

    private WorkerContext() {}

    public static void setWorker(String workerId, String phone) {
        CURRENT_WORKER_ID.set(workerId);
        CURRENT_WORKER_PHONE.set(phone);
    }

    public static String getWorkerId() {
        return CURRENT_WORKER_ID.get();
    }

    public static String getWorkerPhone() {
        return CURRENT_WORKER_PHONE.get();
    }

    public static void clear() {
        CURRENT_WORKER_ID.remove();
        CURRENT_WORKER_PHONE.remove();
    }
}
