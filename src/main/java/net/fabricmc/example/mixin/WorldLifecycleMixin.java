package net.fabricmc.example.mixin;

import net.fabricmc.example.Holder;
import net.minecraft.src.Minecraft;
import net.minecraft.src.WorldClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

@Mixin(Minecraft.class)
public abstract class WorldLifecycleMixin {

    @Shadow
    public File mcDataDir;

    @Shadow
    public net.minecraft.src.ServerData currentServerData;

    @Inject(method = "loadWorld(Lnet/minecraft/src/WorldClient;Ljava/lang/String;)V", at = @At("HEAD"))
    private void onWorldLoad(WorldClient world, String message, CallbackInfo ci) {
        Minecraft mc = (Minecraft) (Object) this;
        if (world == null) {
            // Unloading world/returning to menu - Save data
            if (mc.theWorld != null) {
                File saveDir = getSaveDirectory(mc);
                if (saveDir != null) {
                    Holder.save(saveDir);
                }
            }
        } else {
            // Loading new world
            File saveDir = getSaveDirectory(mc, world);
            if (saveDir != null) {
                Holder.load(saveDir);
            }
        }
    }

    @Inject(method = "shutdown", at = @At("HEAD"))
    private void onShutdown(CallbackInfo ci) {
        Minecraft mc = (Minecraft) (Object) this;
        if (mc.theWorld != null) {
            File saveDir = getSaveDirectory(mc);
            if (saveDir != null) {
                Holder.save(saveDir);
            }
        }
    }

    private File getSaveDirectory(Minecraft mc) {
        if (mc.isSingleplayer()) {
            if (mc.theWorld != null && mc.theWorld.getSaveHandler() != null) {
                String folderName = mc.theWorld.getSaveHandler().getWorldDirectoryName();
                return new File(new File(mcDataDir, "saves"), folderName);
            }
        } else {
            // Multiplayer server-specific folder
            if (currentServerData != null) {
                String serverName = currentServerData.serverIP.replace(":", "_");
                File multiDir = new File(mcDataDir, "saves_multiplayer");
                if (!multiDir.exists())
                    multiDir.mkdirs();
                return new File(multiDir, serverName);
            }
        }
        return null;
    }

    private File getSaveDirectory(Minecraft mc, WorldClient world) {
        if (mc.isSingleplayer()) {
            if (world != null && world.getSaveHandler() != null) {
                String folderName = world.getSaveHandler().getWorldDirectoryName();
                return new File(new File(mcDataDir, "saves"), folderName);
            }
        } else {
            if (currentServerData != null) {
                String serverName = currentServerData.serverIP.replace(":", "_");
                File multiDir = new File(mcDataDir, "saves_multiplayer");
                if (!multiDir.exists())
                    multiDir.mkdirs();
                return new File(multiDir, serverName);
            }
        }
        return null;
    }
}
