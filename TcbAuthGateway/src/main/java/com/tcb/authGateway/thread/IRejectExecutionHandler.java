package com.tcb.authGateway.thread;

public interface IRejectExecutionHandler {
  void rejectExecution(IThreadTask task, ThreadPoolExecutor executor);
}