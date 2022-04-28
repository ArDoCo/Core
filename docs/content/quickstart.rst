Quickstart
======

.. warning:: This site is deprecated

The ArDoCo-Core is a maven project and can be embedded by using its specs (from the `pom <https://github.com/ArDoCo/Core/blob/main/pom.xml>`_).

If you don't want to run the Core by its `main function <https://github.com/ArDoCo/Core/blob/main/src/main/java/modelconnector/Starter.java#L54>`_ you can call `runTest() <https://github.com/ArDoCo/Core/blob/main/src/main/java/modelconnector/Starter.java#L58>`_ or `runDocumentation() <https://github.com/ArDoCo/Core/blob/main/src/main/java/modelconnector/Starter.java#L62>`_ directly.

The `documentation <https://github.com/ArDoCo/Core/blob/main/src/main/resources/config.properties#L2>`_ or `test file <https://github.com/ArDoCo/Core/blob/main/src/main/resources/config.properties#L3>`_ can be specified in the `configuration file <https://github.com/ArDoCo/Core/blob/main/src/main/resources/config.properties>`_.

If you run the Core, all pipeline stages are executed.

The results that should be printed can be specified in the `FilesWriter <https://github.com/ArDoCo/Core/blob/63442034f6fadd34afc8ad36e79322c0acc7cd5a/src/main/java/modelconnector/helpers/FilesWriter.java#L34>`_ and in `run() <https://github.com/ArDoCo/Core/blob/main/src/main/java/modelconnector/Starter.java#L66>`_.

The output file is specified in the `config file <https://github.com/ArDoCo/Core/blob/main/src/main/resources/config.properties#L5>`_.


Please acknowledge the `code of conduct <https://github.com/ArDoCo/Core/blob/main/CODE_OF_CONDUCT.md>`_.


Forking the project & submitting pull requests
-------------

This project uses Sonarcloud to check code quality.
There are Github Actions that automatically verify the build and generate a Sonarcloud-report.
Additionally, pull requests are automatically checked.
If the build fails or the Quality Gate is not passed, it is marked in the Pull Request and you need to fix the PR until it passes.
Otherwise, the PR won't get merged.

If you fork the project, make sure to create a Sonarcloud token to make sure everything works for you and the Sonarcloud check does not fail.
You need to enable Sonarcloud for you and add a Sonarcloud token to the repository of the fork as secret.

Follow the following steps to do so:

1. Log into SonarCloud and click on your profile and then go to ``My Account`` and then ``Security``. Alternatively go directly to ``account/security``.
2. Generate your access token for SonarCloud and copy it. The access token will be provided to the build pipeline as a secret environment variable.
3. Go to your repository settings in Github, then to ``Secrets``
4. Add a new secret with name ``SONAR_TOKEN`` and the value of the just generated access token.


Formatter
^^^^^^

Please use the provided `formatter <https://github.com/ArDoCo/Core/blob/main/formatter.xml>`_ when contributing.

Additionally, make use of the spotless-plugin for maven to format your code. You can run it via ``mvn spotless:apply`` (`more info <https://github.com/diffplug/spotless/tree/main/plugin-maven>`_).




.. toctree::
   :hidden:

   quickstart/cli.rst
   quickstart/saveActions.rst


