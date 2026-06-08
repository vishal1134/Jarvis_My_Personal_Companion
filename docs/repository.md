# Repository

Main GitHub repository:

```text
https://github.com/vishal1134/Jarvis_My_Personal_Companion.git
```

## First Git Setup

After Git is installed and available in the terminal, connect this local project
folder to the GitHub repository:

```powershell
git init
git branch -M main
git remote add origin https://github.com/vishal1134/Jarvis_My_Personal_Companion.git
git add .
git commit -m "Initial Jarvis project foundation"
git push -u origin main
```

If the remote already contains files, pull first:

```powershell
git pull origin main --allow-unrelated-histories
```

