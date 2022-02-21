git remote add -f benchmark git@github.com:ArDoCo/Benchmark.git
git fetch benchmark main
git subtree pull --prefix tests/src/test/resources/benchmark benchmark main --squash
read -n 1 -s -p "Press any key to exit..."