package com.vishal.jarvis;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;

public class FlashlightController {
    public enum Result {
        UPDATED,
        MISSING_PERMISSION,
        UNAVAILABLE
    }

    private final Context context;
    private final CameraManager cameraManager;

    public FlashlightController(Context context) {
        this.context = context;
        this.cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
    }

    public Result setEnabled(boolean enabled) {
        if (context.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return Result.MISSING_PERMISSION;
        }

        try {
            String cameraId = firstBackCameraId();
            if (cameraId == null) {
                return Result.UNAVAILABLE;
            }
            cameraManager.setTorchMode(cameraId, enabled);
            return Result.UPDATED;
        } catch (CameraAccessException exception) {
            return Result.UNAVAILABLE;
        }
    }

    private String firstBackCameraId() throws CameraAccessException {
        String[] cameraIds = cameraManager.getCameraIdList();
        for (String cameraId : cameraIds) {
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            Boolean hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
            if (Boolean.TRUE.equals(hasFlash)) {
                return cameraId;
            }
        }
        return null;
    }
}
