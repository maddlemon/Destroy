package destroytest;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import com.mojang.logging.LogUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Dev-only scripted client driver for comparing the port against 1.20.1. Reads the script named by the
 * {@code destroytest.script} system property and runs one command per tick (or waits). Output goes to
 * {@code <gameDir>/autotest/} and screenshots to {@code <gameDir>/screenshots/}.
 * Version-specific calls live in {@link Compat}; this class is shared between both versions.
 */
@Mod("destroytest")
public class AutoTest {

    static final org.slf4j.Logger LOG = LogUtils.getLogger();

    private final Deque<String> lines = new ArrayDeque<>();
    private int waitTicks = 0;
    private boolean waitingForWorld = false;
    private int worldTicks = 0;
    private Path out;

    public AutoTest(IEventBus modBus) {
        String script = System.getProperty("destroytest.script", "");
        if (script.isEmpty()) return;
        try {
            for (String l : Files.readAllLines(Path.of(script), StandardCharsets.UTF_8)) {
                l = l.strip();
                if (!l.isEmpty() && !l.startsWith("#")) lines.add(l);
            };
        } catch (IOException e) {
            throw new RuntimeException(e);
        };
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post e) -> tick());
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.client.event.ClientChatReceivedEvent e) -> { if (out != null) log("CHAT " + e.getMessage().getString()); });
    };

    private void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (out == null) {
            String script = Path.of(System.getProperty("destroytest.script")).getFileName().toString();
            out = mc.gameDirectory.toPath().resolve("autotest").resolve(script.substring(0, script.lastIndexOf('.')));
            try { Files.createDirectories(out); } catch (IOException e) { throw new RuntimeException(e); };
        };
        if (waitingForWorld) {
            if (mc.player == null || mc.level == null || mc.screen != null && !(mc.screen.isPauseScreen())) { worldTicks = 0; return; };
            if (++worldTicks < 100) return;
            waitingForWorld = false;
        };
        if (waitTicks > 0) { waitTicks--; return; };
        // Don't start until the title screen is up (resources loaded)
        if (mc.level == null && !(mc.screen instanceof TitleScreen) && !(mc.screen instanceof CreateWorldScreen)) return;
        while (!lines.isEmpty() && waitTicks == 0 && !waitingForWorld) {
            String line = lines.poll();
            LOG.info("[autotest] > {}", line);
            try {
                run(mc, line);
            } catch (Throwable t) {
                LOG.error("[autotest] command failed: " + line, t);
                log("FAILED " + line + " :: " + t);
            };
        };
    };

    private void run(Minecraft mc, String line) throws Exception {
        String[] a = line.split(" ", 2);
        String arg = a.length > 1 ? a[1] : "";
        String[] args = arg.isEmpty() ? new String[0] : arg.split(" ");
        if (a[0].equals("v120") || a[0].equals("v121")) {
            if (a[0].equals("v121")) run(mc, arg);
            return;
        };
        switch (a[0]) {
            case "newworld" -> newWorld(mc, arg);
            case "waitworld" -> waitingForWorld = true;
            case "wait" -> waitTicks = Integer.parseInt(arg);
            case "cmd" -> mc.player.connection.sendCommand(arg);
            case "say" -> log(arg);
            case "gui" -> { mc.options.guiScale().set(Integer.parseInt(arg)); mc.resizeDisplay(); }
            case "shot" -> Screenshot.grab(mc.gameDirectory, arg + ".png", mc.getMainRenderTarget(), c -> {});
            case "close" -> mc.setScreen(null);
            case "hud" -> mc.options.hideGui = arg.equals("off");
            case "inv" -> Compat.openInventory(mc);
            case "look" -> { mc.player.setYRot(Float.parseFloat(args[0])); mc.player.setXRot(Float.parseFloat(args[1])); }
            case "hotbar" -> mc.player.getInventory().selected = Integer.parseInt(arg);
            case "use" -> net.minecraft.client.KeyMapping.click(mc.options.keyUse.getKey());
            case "attack" -> net.minecraft.client.KeyMapping.click(mc.options.keyAttack.getKey());
            case "mouse" -> mouse(mc, Double.parseDouble(args[0]), Double.parseDouble(args[1]));
            case "click" -> {
                double x = Double.parseDouble(args[0]), y = Double.parseDouble(args[1]);
                int b = args.length > 2 ? Integer.parseInt(args[2]) : 0;
                mouse(mc, x, y);
                if (mc.screen != null) { mc.screen.mouseClicked(x, y, b); mc.screen.mouseReleased(x, y, b); };
            }
            case "key" -> { if (mc.screen != null) mc.screen.keyPressed(Integer.parseInt(args[0]), 0, args.length > 1 ? Integer.parseInt(args[1]) : 0); }
            case "screen" -> log("screen " + (mc.screen == null ? "none" : mc.screen.getClass().getName()));
            case "jei_r" -> JeiHook.showItem(Compat.item(arg), true);
            case "jei_u" -> JeiHook.showItem(Compat.item(arg), false);
            case "jei_cat" -> JeiHook.showCategory(arg);
            case "ponder" -> Compat.ponder(new ItemStack(Compat.item(arg)));
            case "dump" -> dump(mc, arg);
            case "mixin_audit" -> org.spongepowered.asm.mixin.MixinEnvironment.getCurrentEnvironment().audit();
            case "ponderpack" -> ponderPack(mc);
            case "useon" -> {
                net.minecraft.core.BlockPos pos = new net.minecraft.core.BlockPos(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
                net.minecraft.core.Direction face = args.length > 3 ? net.minecraft.core.Direction.byName(args[3]) : net.minecraft.core.Direction.UP;
                var hit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(pos).relative(face, 0.5), face, pos, false);
                log("useon " + arg + " -> " + mc.gameMode.useItemOn(mc.player, net.minecraft.world.InteractionHand.MAIN_HAND, hit));
            }
            case "be" -> {
                net.minecraft.core.BlockPos pos = new net.minecraft.core.BlockPos(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
                var server = mc.getSingleplayerServer();
                server.execute(() -> {
                    var be = server.overworld().getBlockEntity(pos);
                    log("BE " + arg + " " + server.overworld().getBlockState(pos) + " " + (be == null ? "none" : Compat.saveBE(be, server)));
                });
            }
            case "drop" -> {
                var server = mc.getSingleplayerServer();
                double x = Double.parseDouble(args[0]), y = Double.parseDouble(args[1]), z = Double.parseDouble(args[2]);
                ItemStack stack = new ItemStack(Compat.item(args[3]), args.length > 4 ? Integer.parseInt(args[4]) : 1);
                server.execute(() -> {
                    var e = new net.minecraft.world.entity.item.ItemEntity(server.overworld(), x, y, z, stack);
                    e.setDeltaMovement(0, 0, 0);
                    server.overworld().addFreshEntity(e);
                });
            }
            case "items" -> {
                var server = mc.getSingleplayerServer();
                double x = Double.parseDouble(args[0]), y = Double.parseDouble(args[1]), z = Double.parseDouble(args[2]), r = Double.parseDouble(args[3]);
                server.execute(() -> {
                    var box = new net.minecraft.world.phys.AABB(x - r, y - r, z - r, x + r, y + r, z + r);
                    java.util.Map<String, Integer> counts = new java.util.TreeMap<>();
                    for (var e : server.overworld().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, box))
                        counts.merge(id(e.getItem().getItem()), e.getItem().getCount(), Integer::sum);
                    log("ITEMS " + arg + " " + counts);
                });
            }
            case "protection" -> {
                var server = mc.getSingleplayerServer();
                server.execute(() -> {
                    try {
                        Class<?> c;
                        try { c = Class.forName("petrolpark.mc.destroy.core.chemistry.hazard.ChemistryHazardHelper$Protection"); }
                        catch (ClassNotFoundException e) { c = Class.forName("com.petrolpark.destroy.core.chemistry.hazard.ChemistryHazardHelper$Protection"); };
                        var player = server.getPlayerList().getPlayers().get(0);
                        Method m = c.getMethod("isProtected", net.minecraft.world.entity.LivingEntity.class);
                        List<String> res = new ArrayList<>();
                        for (Object p : c.getEnumConstants()) res.add(p + "=" + m.invoke(p, player));
                        log("PROTECTION " + arg + " " + res);
                    } catch (Exception e) { log("PROTECTION failed " + e); };
                });
            }
            case "quit" -> mc.stop();
            default -> log("UNKNOWN " + line);
        };
    };

    /** Copies the mod's Ponder structures into the world as a datapack ("destroytest:<path>") so scripts can /place them */
    private void ponderPack(Minecraft mc) throws IOException {
        Path pack = mc.getSingleplayerServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.DATAPACK_DIR).resolve("autotest");
        Files.createDirectories(pack);
        Files.writeString(pack.resolve("pack.mcmeta"), "{\"pack\":{\"pack_format\":" + Compat.DATA_PACK_FORMAT + ",\"description\":\"autotest\"}}");
        int n = 0;
        for (var e : mc.getResourceManager().listResources("ponder", rl -> rl.getNamespace().equals("destroy") && rl.getPath().endsWith(".nbt")).entrySet()) {
            Path target = pack.resolve("data/destroytest/" + Compat.STRUCTURE_DIR + "/" + e.getKey().getPath().substring("ponder/".length()));
            Files.createDirectories(target.getParent());
            try (var in = e.getValue().open()) { Files.copy(in, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING); };
            n++;
        };
        log("ponderpack: " + n + " structures");
    };

    private void mouse(Minecraft mc, double guiX, double guiY) throws Exception {
        double s = mc.getWindow().getGuiScale();
        Method m = mc.mouseHandler.getClass().getDeclaredMethod("onMove", long.class, double.class, double.class);
        m.setAccessible(true);
        m.invoke(mc.mouseHandler, mc.getWindow().getWindow(), guiX * s, guiY * s);
    };

    private void newWorld(Minecraft mc, String name) throws Exception {
        CreateWorldScreen.openFresh(mc, mc.screen);
        CreateWorldScreen screen = (CreateWorldScreen) mc.screen;
        WorldCreationUiState ui = screen.getUiState();
        ui.setName(name);
        ui.setSeed("20261021");
        ui.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);
        Compat.allowCommands(ui);
        for (WorldCreationUiState.WorldTypeEntry e : ui.getNormalPresetList())
            if (e.preset() != null && e.preset().unwrapKey().map(k -> k.location().getPath().equals("flat")).orElse(false)) ui.setWorldType(e);
        Method onCreate = CreateWorldScreen.class.getDeclaredMethod("onCreate");
        onCreate.setAccessible(true);
        onCreate.invoke(screen);
        waitingForWorld = true;
    };

    private void dump(Minecraft mc, String what) throws Exception {
        List<String> rows = new ArrayList<>();
        switch (what) {
            case "items" -> Dumps.items(mc, rows);
            case "blocks" -> Dumps.blocks(mc, rows);
            case "fluids" -> Dumps.fluids(rows);
            case "creative" -> Dumps.creative(mc, rows);
            case "recipes" -> Compat.recipes(mc, rows);
            case "enchant" -> Compat.enchantments(mc, rows);
            case "compost" -> Compat.compost(rows);
            case "tags" -> Dumps.tags(rows);
            case "jei" -> JeiHook.dumpCategories(rows);
            case "jei_ingredients" -> JeiHook.dumpIngredients(rows);
            case "jei_recipes" -> JeiHook.dumpRecipes(rows);
            default -> rows.add("unknown dump " + what);
        };
        try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(out.resolve(what + ".tsv"), StandardCharsets.UTF_8))) {
            rows.forEach(w::println);
        };
        log("dumped " + what + " (" + rows.size() + " rows)");
    };

    private void log(String s) {
        LOG.info("[autotest] {}", s);
        try {
            Files.writeString(out.resolve("log.txt"), s + System.lineSeparator(), StandardCharsets.UTF_8,
                java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException(e);
        };
    };

    static boolean isDestroy(net.minecraft.resources.ResourceLocation rl) {
        return rl != null && rl.getNamespace().equals("destroy");
    };

    static String id(net.minecraft.world.item.Item item) {
        return BuiltInRegistries.ITEM.getKey(item).toString();
    };
}
