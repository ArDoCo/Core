Quickstart
=============

The ArDoCo-Core is a maven project and can be embedded by using its specs (from the `pom <https://github.com/ArDoCo/Core/blob/main/pom.xml>`_).

You can run and configure the execution with the :doc:`CLI <quickstart/cli>`.

Please acknowledge the `code of conduct <https://github.com/ArDoCo/Core/blob/main/CODE_OF_CONDUCT.md>`_.


Forking the project & submitting pull requests
------------------------------------------------

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
^^^^^^^^^^^^^^

Please use the provided `formatter <https://github.com/ArDoCo/Core/blob/main/formatter.xml>`_ when contributing.

Additionally, make use of the spotless-plugin for maven to format your code. You can run it via ``mvn spotless:apply`` (`more info <https://github.com/diffplug/spotless/tree/main/plugin-maven>`_).

Documentation
----------------

Please update the documentation.

This wiki is written in restructured Text. Here, you find more `information and guidelines <https://www.sphinx-doc.org/en/master/usage/restructuredtext/index.html>`_.

We use `PlantUML <https://plantuml.com/de/>`_ for the diagrams.
This is a good `installation guide <https://sphinxcontrib-needs.readthedocs.io/en/latest/installation.html>`_.
`This <https://chiplicity.readthedocs.io/en/latest/Using_Sphinx/UsingGraphicsAndDiagramsInSphinx.html>`_ is another helpful website with many examples.

More information about the used theme are available on the `website of Furo <https://pradyunsg.me/furo/>`_.


.. toctree::
   :hidden:

   quickstart/cli.rst
   quickstart/standardConfiguration.rst
   quickstart/saveActions.rst


