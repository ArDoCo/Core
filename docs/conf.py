# Configuration file for the Sphinx documentation builder.
#
# This file only contains a selection of the most common options. For a full
# list see the documentation:
# https://www.sphinx-doc.org/en/master/usage/configuration.html

# -- Path setup --------------------------------------------------------------

# If extensions (or modules to document with autodoc) are in another directory,
# add these directories to sys.path here. If the directory is relative to the
# documentation root, use os.path.abspath to make it absolute, like shown here.
#
# import os
# import sys
# sys.path.insert(0, os.path.abspath('.'))


# -- Project information -----------------------------------------------------
import os

project = 'ArDoCo'
copyright = '2022, Sophie Corallo, Jan Keim, Dominik Fuchß'
author = 'Sophie Corallo, Jan Keim, Dominik Fuchß'

# The full version, including alpha/beta/rc tags
release = '0.3'

# -- General configuration ---------------------------------------------------

# Add any Sphinx extension module names here, as strings. They can be
# extensions coming with Sphinx (named 'sphinx.ext.*') or your custom
# ones.
extensions = ['sphinxcontrib.plantuml']

is_ci = os.environ.get("CI") == "true"
if is_ci:
    plantuml = '/usr/bin/java -jar %s' % os.path.join(os.path.dirname(__file__), "bin", "plantuml.jar")
else:
    plantuml = 'java -jar %s' % os.path.join(os.path.dirname(__file__), "bin", "plantuml.jar")
plantuml_output_format = "svg_img"

# Add any paths that contain templates here, relative to this directory.
templates_path = ['_templates']

# List of patterns, relative to source directory, that match files and
# directories to ignore when looking for source files.
# This pattern also affects html_static_path and html_extra_path.
exclude_patterns = ['_build', 'Thumbs.db', '.DS_Store']

# -- Options for HTML output -------------------------------------------------

# The theme to use for HTML and HTML Help pages.  See the documentation for
# a list of builtin themes.
#
html_theme = 'furo'



# Add any paths that contain custom static files (such as style sheets) here,
# relative to this directory. They are copied after the builtin static files,
# so a file named "default.css" will overwrite the builtin "default.css".
html_static_path = ['_static']

html_title = " "

html_theme_options = {
    "light_logo": "ArDoCo-light.svg",
    "dark_logo": "ArDoCo-dark.svg",
    "navigation_with_keys": True,
}


on_rtd = os.environ.get('READTHEDOCS') == 'True'
if on_rtd:
    plantuml = 'java -Djava.awt.headless=true -jar /usr/share/plantuml/plantuml.jar'
else:
    plantuml = 'java -jar %s' % os.path.join(os.path.dirname(__file__), "utils", "plantuml.jar")

plantuml_output_format = 'png'