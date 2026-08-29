package com.astroboii47.commander;

import android.graphics.Bitmap;

interface ITaskSwitcherService {
    Bitmap getTaskSnapshot(int taskId, int width, int height, boolean captureNow);
    boolean switchToTask(int taskId, int rootTaskId);
    boolean removeTask(int taskId);
}
