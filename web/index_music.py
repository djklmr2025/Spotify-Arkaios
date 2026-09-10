#!/usr/bin/env python3
import os
import json
import hashlib

music_dir = r"C:\DJKLMR\Music"
tracks = []
valid_exts = {".mp3", ".wav", ".m4a", ".flac", ".aac", ".ogg"}

print(f"Indexando directorio: {music_dir}...")
count = 0
for root, dirs, files in os.walk(music_dir):
    for f in files:
        ext = os.path.splitext(f)[1].lower()
        if ext in valid_exts:
            full_path = os.path.join(root, f)
            try:
                size = os.path.getsize(full_path)
            except Exception:
                size = 0
            base_name = os.path.splitext(f)[0]
            if " - " in base_name:
                parts = base_name.split(" - ", 1)
                artist = parts[0].strip()
                title = parts[1].strip()
            else:
                artist = "DJ KLMR"
                title = base_name.strip()
            
            track_id = "klmr_" + hashlib.md5(full_path.encode("utf-8")).hexdigest()[:10]
            tracks.append({
                "id": track_id,
                "title": title,
                "artist": artist,
                "album": "DJ KLMR Private Vault",
                "genre": "DJ Edit / Urban Remix",
                "format": ext.replace(".", "").upper(),
                "duration": "3:45",
                "sizeBytes": size,
                "filePath": full_path,
                "streamUrl": f"/api/stream/{track_id}",
                "cover": "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600"
            })
            count += 1
            if count % 2000 == 0:
                print(f"  Progreso: {count} pistas indexadas...")

out_file = r"C:\ARKAIOS\Spotify-Arkaios\web\arkaios_music_db.json"
with open(out_file, "w", encoding="utf-8") as fp:
    json.dump(tracks, fp, ensure_ascii=False)

print(f"[OK COMPLETO] Total tracks indexados: {len(tracks)} guardados en: {out_file}")
