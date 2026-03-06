#!/usr/bin/env bash
set -euo pipefail

REQUIRED_TOOLS=("sed" "grep" "cut" "tr" "git")
for tool in "${REQUIRED_TOOLS[@]}"; do
    if ! command -v "$tool" &>/dev/null; then
        echo "::error::Required tool '$tool' not found" >&2
        exit 1
    fi
done

build_gradle_content=$(cat build.gradle)
gradle_properties_content=$(cat gradle.properties)

# Get the mods java version
java_ver=$(sed -n 's/.*JavaLanguageVersion.of(\([0-9]*\)).*/\1/p' <<<"$build_gradle_content")
echo "java_version=$java_ver" >>"$GITHUB_OUTPUT"
echo "java_version=$java_ver" >>"$GITHUB_STEP_SUMMARY"

# Get the mod id
mod_id=$(grep "^mod_id=" <<<"$gradle_properties_content" | cut -d'=' -f2 | tr -d '\r')
echo "mod_id=$mod_id" >>"$GITHUB_OUTPUT"
echo "mod_id=$mod_id" >>"$GITHUB_STEP_SUMMARY"

# Get mod name
mod_name=$(grep "^mod_name=" <<<"$gradle_properties_content" | cut -d'=' -f2 | tr -d '\r')
echo "mod_name=$mod_name" >>"$GITHUB_OUTPUT"
echo "mod_name=$mod_name" >>"$GITHUB_STEP_SUMMARY"

# Get the Minecraft version
minecraft_version=$(grep "^minecraft_version=" <<<"$gradle_properties_content" | cut -d'=' -f2 | tr -d '\r')
echo "minecraft_version=$minecraft_version" >>"$GITHUB_OUTPUT"
echo "minecraft_version=$minecraft_version" >>"$GITHUB_STEP_SUMMARY"

# Get the Mod version
mod_version=$(grep "^mod_version=" <<<"$gradle_properties_content" | cut -d'=' -f2 | tr -d '\r')
echo "mod_version=$mod_version" >>"$GITHUB_OUTPUT"
echo "mod_version=$mod_version" >>"$GITHUB_STEP_SUMMARY"

# Get the current tag
if ! current_git_tag=$(git describe --tags --abbrev=0 2>/dev/null); then
    echo "No git tag found. Not a tagged commit" >&2
    exit 0
fi
# Everything above is used in build-commit and build-release
# Everything below is used in build-release

# Get the branch name, so that we can extract the mod_loader
raw_branch=$(git branch -r --contains HEAD | grep -v "HEAD" | head -n 1 | xargs || echo "")
branch_name=${raw_branch#origin/} # Remove 'origin/' prefix
mod_loader=${branch_name##*-}
echo "mod_loader=$mod_loader" >>"$GITHUB_OUTPUT"
echo "mod_loader=$mod_loader" >>"$GITHUB_STEP_SUMMARY"

# Get mod release type
mod_release_type=${current_git_tag%%/*}
# Check that the release type is valid (release, beta, alpha)
case "$mod_release_type" in release | beta | alpha) ;; *)
    echo "::error::Invalid release type '$mod_release_type' expected either release, beta or alpha" >&2
    exit 1
    ;;
esac
echo "mod_release_type=$mod_release_type" >>"$GITHUB_OUTPUT"
echo "mod_release_type=$mod_release_type" >>"$GITHUB_STEP_SUMMARY"

# Get the previous tag
if ! previous_tag=$(git describe --tags --abbrev=0 --match "*/v*" HEAD^ 2>/dev/null); then
    previous_tag=""
    previous_mod_version="none"
else
    previous_mod_version=$(git show "$previous_tag:gradle.properties" | grep "^mod_version=" | cut -d'=' -f2 | tr -d '\r' || echo "none")
fi

echo "previous_tag=$previous_tag" >>"$GITHUB_OUTPUT"
echo "previous_mod_version=$previous_mod_version" >>"$GITHUB_OUTPUT"

# Generate changelog
if [ -z "$previous_tag" ]; then
    git_log_range="$current_git_tag"
else
    git_log_range="$previous_tag..$current_git_tag"
fi

# Build the changelog
repo="github.com/$GITHUB_REPOSITORY"
echo "changelog<<EOF" >>"$GITHUB_OUTPUT"
git log "$git_log_range" --pretty=format:"[%s](https://$repo/commit/%H) - %an<br>" >>"$GITHUB_OUTPUT"
echo "" >>"$GITHUB_OUTPUT"
echo "EOF" >>"$GITHUB_OUTPUT"

echo "git_log_range=$git_log_range" >>"$GITHUB_STEP_SUMMARY"
