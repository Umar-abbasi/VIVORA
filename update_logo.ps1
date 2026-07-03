$src = "C:\Users\Abbasi\Downloads\vivora_icon_1024.png"
$densities = @("mdpi", "hdpi", "xhdpi", "xxhdpi", "xxxhdpi")

foreach ($density in $densities) {
    $destDir = "f:\Alarm app\app\src\main\res\mipmap-$density"
    if (Test-Path $destDir) {
        Copy-Item $src "$destDir\ic_launcher.png" -Force
        Copy-Item $src "$destDir\ic_launcher_round.png" -Force
    }
}
