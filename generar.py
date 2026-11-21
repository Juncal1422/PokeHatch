"""Construye el HTML que descarga el lanzador de PokeHatch."""
from pathlib import Path
import base64
import json
import re

ROOT = Path(__file__).resolve().parent

def read(path):
    return (ROOT / path).read_text(encoding="utf-8")

def embedded(path, mime):
    payload = base64.b64encode(path.read_bytes()).decode("ascii")
    return f"data:{mime};base64,{payload}"

def build():
    database = {path.stem: json.loads(read(path)) for path in sorted((ROOT / "data").glob("*.json"))}
    art = {}
    for path in sorted((ROOT / "assets").rglob("*.png")):
        if path.stem in art:
            raise ValueError(f"Clave de imagen duplicada: {path.stem}")
        art[path.stem] = embedded(path, "image/png")
    database["art"] = art
    scripts = json.loads(read("src/orden.json"))
    css = re.sub(r"url\(\.\./(assets/fuentes/[^)]+)\)",
                 lambda m: "url(" + embedded(ROOT / m[1], "font/ttf") + ")",
                 read("styles/juego.css"))
    result = read("src/plantilla.html").replace("{{CSS}}", css)
    result = result.replace("{{DATABASE}}", json.dumps(database, ensure_ascii=False, separators=(",", ":")).replace("<", r"\u003c"))
    result = result.replace("{{GAME}}", "\n".join(read(path) for path in scripts))
    destination = ROOT / "PokeHatch v1.0.html"
    destination.write_text(result, encoding="utf-8")
    print(destination)

if __name__ == "__main__":
    build()
