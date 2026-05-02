# Demo Playbook: AirPods Price Comparison (Amazon vs Walmart)

## Prerequisites

- Emulator running: `emulator-5554`
- Apps installed: Amazon Shopping (`com.amazon.mShop.android.shopping`)
- Browser available: Firefox Beta (`org.mozilla.firefox_beta`) — pre-installed on AOSP emulator
- Walmart: use walmart.com via browser (Walmart native app has CAPTCHA that blocks agent)
- OpenCyvis deployed with Qwen config: `deploy-emu.sh qwen`
- Model: `qwen3.6-plus-2026-04-02`
- Config file: `~/.config/opencyvis/.env`
- macOS emulator window ID: find via `python3 -c "import Quartz; ..."`

## Task Instruction

```
Compare AirPods Pro prices: first check on Amazon app, then open walmart.com in the browser to compare. Tell me which is cheaper.
```

## Typical Result (~90s)

- Amazon: AirPods Pro 2nd Gen - $244.00
- Walmart: Apple AirPods Pro 2 - $199.00 (was $239.00)
- Conclusion: Walmart is cheaper by $45.00

## Recording A: ADB screenrecord (ControlPanel view, no glow)

Shows the ControlPanel UI with typing animation, chat bubbles, and status updates.
Does NOT capture overlay glow effect.

```bash
# 1. Fresh start
adb -s emulator-5554 shell "am force-stop ai.opencyvis"
adb -s emulator-5554 shell "pm clear ai.opencyvis"
cd ~/aiphone/android && ./deploy-emu.sh qwen

# 2. Open ControlPanel
sleep 3
adb -s emulator-5554 shell "am start -n ai.opencyvis/.ui.ControlPanelActivity"
sleep 2

# 3. Start recording (300s max, trim later)
adb -s emulator-5554 shell "screenrecord --time-limit 300 /sdcard/demo_airpods_adb.mp4" &

# 4. Send task (wait 3s for recording to start)
sleep 3
adb -s emulator-5554 shell "am broadcast -a ai.opencyvis.TEST -p ai.opencyvis \
  --es demo 'Compare AirPods Pro prices: first check on Amazon app, then open walmart.com in the browser to compare. Tell me which is cheaper.' \
  --el char_delay 40"

# 5. Wait for task to complete (monitor logcat or wait ~3-5 min)
#    Check: adb logcat | grep -i "complet"

# 6. Stop recording after task completes
adb -s emulator-5554 shell "pkill -INT screenrecord"
sleep 3

# 7. Pull and trim
adb -s emulator-5554 pull /sdcard/demo_airpods_adb.mp4 ./demo_airpods_adb_raw.mp4
# Trim: ffmpeg -y -i demo_airpods_adb_raw.mp4 -t <seconds> -c copy demo_airpods_adb.mp4
```

## Recording B: macOS screencapture + View mode (with glow)

Shows the agent's virtual display through View mode.
Captures overlay glow effect around the emulator window.

```bash
# 1. Fresh start (same as above)
adb -s emulator-5554 shell "am force-stop ai.opencyvis"
adb -s emulator-5554 shell "pm clear ai.opencyvis"
cd ~/aiphone/android && ./deploy-emu.sh qwen

# 2. Open ControlPanel
sleep 3
adb -s emulator-5554 shell "am start -n ai.opencyvis/.ui.ControlPanelActivity"
sleep 2

# 3. Send task
adb -s emulator-5554 shell "am broadcast -a ai.opencyvis.TEST -p ai.opencyvis \
  --es demo 'Compare AirPods Pro prices: first check on Amazon app, then open walmart.com in the browser to compare. Tell me which is cheaper.' \
  --el char_delay 40"

# 4. Wait for typing animation + first LLM call (~10s)
sleep 10

# 5. Start macOS recording (auto-stops at time limit)
WINDOW_ID=12605  # find via python3 Quartz script
screencapture -V 300 -l $WINDOW_ID ./demo_airpods_view.mov &
CAPTURE_PID=$!

# 6. Tap View button to enter View mode (shows virtual display + glow)
sleep 2
adb -s emulator-5554 shell "input tap 285 1512"

# 7. Wait for task to complete or timeout
wait $CAPTURE_PID

# 8. Convert MOV to MP4
ffmpeg -y -i demo_airpods_view.mov -c:v libx264 -preset fast -crf 23 -pix_fmt yuv420p demo_airpods_view.mp4
```

## View button coordinates

Found via `adb shell uiautomator dump`:
- **View**: bounds [48,1452][522,1572] → tap (285, 1512)
- **Take over**: bounds [388,1596][692,1728] → tap (540, 1662)
- **Back**: bounds [48,1596][352,1728] → tap (200, 1662)
- **Stop**: bounds [558,1452][1032,1572] → tap (795, 1512)

## Post-processing

```bash
# Trim to content duration
ffmpeg -y -i input.mp4 -t <seconds> -c copy output_trimmed.mp4

# Send via Lark for review
cd <output_dir>
lark-cli im +messages-send --as bot --user-id ou_aba5ea4ad6f9605aaeb37f4523c70284 \
  --video ./output.mp4 --video-cover ./cover.jpg
```

## Notes

- `adb screencap`/`screenrecord` cannot capture overlay layers (glow border)
- macOS `screencapture -V <seconds> -l <windowID>` captures everything including overlays
- Glow border only shows while agent task is in progress (not after completion)
- Use `kill -INT <pid>` to stop screenrecord gracefully (SIGKILL corrupts files)
- screencapture `-V` auto-stops at the time limit; `kill -INT` may lose the file
- Walmart native app (`com.walmart.android`) has bot-detection CAPTCHA — use browser instead
- Firefox Beta package: `org.mozilla.firefox_beta` (NOT `org.mozilla.firefox`)
- Task typically completes in ~90s with Qwen 3.6 Plus

## Output Files

- `demo_airpods_controlpanel.mp4` — Recording A (ADB, ControlPanel view)
- `demo_airpods_view.mp4` — Recording B (macOS, View mode with glow)
- `demo_airpods_controlpanel_raw.mp4` — untrimmed Recording A
