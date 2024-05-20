RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m'

for file in $(find winzig_test_programs -type f ! -name "*.*")
do
  ./scripts/run.sh -ast "$file" > test.tree
  ((diff test.tree "$file.tree") && (echo -e "✅ ${GREEN}Passed:${NC} $file")) || (echo -e "❌ ${RED}Failed:${NC} $file")
done
