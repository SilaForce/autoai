import os

def generate_tree(dir_path, prefix=""):
    # Foldere koje želimo ignorisati
    ignore_dirs = {'.git', '.idea', '.gradle', 'build', 'captures', 'out', '.cxx'}
    output = ""

    try:
        items = os.listdir(dir_path)
    except PermissionError:
        return ""

    items = [i for i in items if i not in ignore_dirs]
    items.sort(key=lambda x: (not os.path.isdir(os.path.join(dir_path, x)), x.lower()))

    for i, item in enumerate(items):
        path = os.path.join(dir_path, item)
        is_last = i == (len(items) - 1)
        connector = "└── " if is_last else "├── "

        output += f"{prefix}{connector}{item}\n"

        if os.path.isdir(path):
            extension = "    " if is_last else "│   "
            output += generate_tree(path, prefix + extension)

    return output

def get_file_contents(dir_path):
    ignore_dirs = {'.git', '.idea', '.gradle', 'build', 'captures', 'out', '.cxx'}
    # Ekstenzije fajlova čiji kod želimo da izvučemo
    allowed_extensions = {'.kt', '.java', '.xml', '.gradle', '.kts', '.properties'}
    output = ""

    for root, dirs, files in os.walk(dir_path):
        # Modifikujemo listu direktorijuma na licu mjesta (in-place) da os.walk preskoči ignorisane
        dirs[:] = [d for d in dirs if d not in ignore_dirs]

        for file in files:
            _, ext = os.path.splitext(file)
            if ext in allowed_extensions:
                file_path = os.path.join(root, file)
                rel_path = os.path.relpath(file_path, dir_path)

                try:
                    with open(file_path, 'r', encoding='utf-8') as f:
                        content = f.read()

                    # Mapiranje ekstenzija za Markdown označavanje jezika
                    if ext in ['.kt', '.kts']:
                        lang = "kotlin"
                    elif ext == '.java':
                        lang = "java"
                    elif ext == '.xml':
                        lang = "xml"
                    else:
                        lang = "text"

                    output += f"\n### Fajl: `{rel_path}`\n"
                    output += f"```{lang}\n"
                    output += content
                    output += "\n```\n"

                except Exception as e:
                    output += f"\n### Fajl: `{rel_path}`\n"
                    output += f"> **Greška pri čitanju fajla:** {e}\n"

    return output

if __name__ == "__main__":
    project_root = "."
    output_file = "struktura_i_kod_projekta.md"

    print("Izvlačim strukturu projekta i kod, molim te sačekaj...")

    # 1. Dio: Generisanje strukture
    md_content = "# Struktura Android Projekta\n\n"
    md_content += f"```text\n{os.path.basename(os.path.abspath(project_root))}\n"
    md_content += generate_tree(project_root)
    md_content += "```\n\n"

    md_content += "---\n\n"

    # 2. Dio: Izvlačenje koda iz fajlova
    md_content += "# Sadržaj Fajlova (Kod)\n"
    md_content += get_file_contents(project_root)

    # Upisivanje svega u .md fajl
    with open(output_file, "w", encoding="utf-8") as f:
        f.write(md_content)

    print(f"Gotovo! Sve je sačuvano u fajl: {output_file}")