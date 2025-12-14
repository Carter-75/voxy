package me.cortex.voxy.client.mixin.compat.lsdc;

import me.cortex.voxy.common.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Method;
import java.util.List;

@Pseudo
@Mixin(targets = "loongly.lsdc.common.client.gui.LSDCGameOptionPages", remap = false)
public abstract class MixinLSDCGameOptionPages {
    @Unique
    private static boolean voxy$loggedCpuInfoFailure;

    @Inject(method = "initCPUPages", at = @At("HEAD"), cancellable = true)
    private static void voxy$skipCpuPages(List<?> groups, CallbackInfo ci) {
        try {
            Class<?> systemInfoClass = Class.forName("loongly.lsdc.common.api.system.SystemAndGLInfo");
            Method getInstance = systemInfoClass.getMethod("getInstance");
            Object systemInfo = getInstance.invoke(null);
            if (systemInfo == null) {
                voxy$logAndCancel(ci, "system info provider unavailable; skipping LSDC CPU page");
                return;
            }

            Method getCpuInfo = systemInfoClass.getMethod("getCpuInfo");
            Object cpuInfo = getCpuInfo.invoke(systemInfo);
            if (cpuInfo == null) {
                voxy$logAndCancel(ci, "CPU info unavailable; skipping LSDC CPU page");
            }
        } catch (ClassNotFoundException ignored) {
            // LSDC is not present, nothing to do.
        } catch (ReflectiveOperationException e) {
            Logger.error("Failed to query LSDC CPU info via reflection", e);
        }
    }

    @Unique
    private static void voxy$logAndCancel(CallbackInfo ci, String reason) {
        if (!voxy$loggedCpuInfoFailure) {
            Logger.warn("Mod Menu integration: " + reason);
            voxy$loggedCpuInfoFailure = true;
        }
        ci.cancel();
    }
}
