ArDoCo uses maven profiles to provide subsets of its functionality and speed up development time.

## Current Profiles

* **complete** (activated by default)
* **deployment** (profile for deployment to maven central)
* **tlr** (profile for traceability link recovery)
* **inconsistency** (profile for inconsistency detection)

## Adding new profiles

In order to add a new profile, you have to extend the profile section in the main pom.xml (as well as in all submodules
that contain submodules; i.e., stages, tests)

```xml

<profile>
    <!-- Name of the new Profile -->
    <id>new-profile-id</id>
    <activation>
        <activeByDefault>false</activeByDefault>
    </activation>
    <modules>
        <module>framework</module>
        <module>pipeline</module>
        <module>stages</module>
        <module>tests</module>
    </modules>
</profile>
```


