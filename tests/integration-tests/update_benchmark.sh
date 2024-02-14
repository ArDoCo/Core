git remote add -f benchmark git@github.com:ArDoCo/Benchmark.git
git fetch benchmark main
git subtree pull --prefix tests-base/src/main/resources/benchmark benchmark main --squash

read -n 1 -s -p "Press any key to exit..."
