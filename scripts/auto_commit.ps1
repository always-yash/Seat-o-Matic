# Auto-commit & push helper for Seat-o-Matic repository
# --------------------------------------------------------------
# This script monitors the git working tree for changes, determines the
# relevant module(s) based on changed file paths, switches to the appropriate
# branch (if not already on it), stages the changes, creates a descriptive
# commit, and pushes to origin.
#
# Assumptions:
#   * The repository follows the modular branch naming convention:
#       module/m1-core, module/m2-student, module/m3-exam, module/m4-paper,
#       module/m5-reports, develop (default integration branch).
#   * Each top‑level source folder under src/main/java maps to a module:
#       com/lostfound/member4_claims      -> m4-paper
#       com/lostfound/sudent_module       -> m2-student
#       com/lostfound/xyz (others)       -> appropriate module or develop.
#   * The script is invoked manually or via a scheduled task (see /schedule).
#
# Usage:
#   powershell -ExecutionPolicy Bypass -File "scripts/auto_commit.ps1"
#   (Or add a scheduled task that runs the script periodically.)
#
# Note: The script performs a fast check (git diff --name-only) and does not
# monitor file system events in real‑time. For continuous monitoring, integrate
# it with a file‑watcher (e.g., `fswatch` or PowerShell `Register-ObjectEvent`).
#
# ----------------------------------------------------------------
# Configuration: map folder prefixes to branch names
$folderToBranch = @{
    "src/main/java/com/lostfound/member4_claims" = "module/m4-paper"
    "src/main/java/com/lostfound/sudent_module"  = "module/m2-student"
    "src/main/java/com/lostfound/"               = "module/m1-core"   # fallback for core utilities
}

# Helper: get the current git branch
function Get-CurrentBranch {
    $branch = git rev-parse --abbrev-ref HEAD 2>$null
    return $branch.Trim()
}

# Helper: determine target branch from a list of changed files
function Resolve-TargetBranch($changedFiles) {
    $candidateBranches = @()
    foreach ($file in $changedFiles) {
        foreach ($prefix in $folderToBranch.Keys) {
            if ($file -like "$prefix*") {
                $candidateBranches += $folderToBranch[$prefix]
                break
            }
        }
    }
    $unique = $candidateBranches | Sort-Object -Unique
    if ($unique.Count -eq 0) { return "develop" } # default
    if ($unique.Count -eq 1) { return $unique[0] }
    # Multiple modules affected – stay on current branch and commit collectively
    return $null
}

# Main execution
Write-Host "--- Seat-o-Matic Auto Commit ---" -ForegroundColor Cyan
# Ensure we are inside a git repo
if (-not (Test-Path ".git")) {
    Write-Error "Not a git repository. Abort."
    exit 1
}

# Get list of changed (staged or unstaged) files
$changed = git diff --name-only
if (-not $changed) {
    Write-Host "No changes detected. Exiting."
    exit 0
}
$changedArray = $changed -split "`n" | Where-Object { $_ -ne "" }
Write-Host "Changed files:`n" ($changedArray -join "`n")

$targetBranch = Resolve-TargetBranch $changedArray
$currentBranch = Get-CurrentBranch

if ($null -ne $targetBranch) {
    if ($currentBranch -ne $targetBranch) {
        Write-Host "Switching from '$currentBranch' to target branch '$targetBranch'..."
        git checkout $targetBranch
        if ($LASTEXITCODE -ne 0) {
            Write-Error "Failed to checkout branch $targetBranch. Abort."
            exit 1
        }
    } else {
        Write-Host "Already on target branch '$targetBranch'."
    }
} else {
    Write-Host "Changes span multiple modules. Staying on current branch '$currentBranch'."
}

# Stage all changed files
git add .
if ($LASTEXITCODE -ne 0) { Write-Error "git add failed"; exit 1 }

# Build a concise commit message
$module = if ($targetBranch) { $targetBranch.Split('/')[-1] } else { "mixed-modules" }
$shortMsg = "[Auto] Update $module: $(($changedArray | Measure-Object).Count) file(s)"
$fullMsg = "Automatic commit triggered by auto_commit.ps1`n`nChanged files:`n" + ($changedArray -join "`n")

git commit -m $shortMsg -m $fullMsg
if ($LASTEXITCODE -ne 0) { Write-Error "git commit failed"; exit 1 }

# Push to origin
Write-Host "Pushing to origin/$targetBranch (or current branch)..."
git push origin $currentBranch
if ($LASTEXITCODE -ne 0) { Write-Error "git push failed"; exit 1 }

Write-Host "Commit and push completed successfully." -ForegroundColor Green

# End of script

