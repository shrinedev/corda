Building and installing a CorDapp
=================================

.. contents::

Cordapps run on the Corda platform and integrate with it and each other. This article explains how to build CorDapps.
To learn what a CorDapp is, please read :doc:`cordapp-overview`.

CorDapp format
--------------
A CorDapp is a semi-fat JAR that contains all of the CorDapp's dependencies *except* the Corda core libraries and any
other CorDapps it depends on.

For example, if a Cordapp depends on ``corda-core``, ``your-other-cordapp`` and ``apache-commons``, then the Cordapp
JAR will contain:

* All classes and resources from the ``apache-commons`` JAR and its dependencies
* *Nothing* from the other two JARs

Build tools
-----------
In the instructions that follow, we assume you are using Gradle and the ``cordformation`` plugin to build your
CorDapp. You can find examples of building a CorDapp using these tools in the 
`Kotlin CorDapp Template <https://github.com/corda/cordapp-template-kotlin>`_ and the 
`Java CorDapp Template <https://github.com/corda/cordapp-template-kotlin>`_.

To ensure you are using the correct version of Gradle, you should use the provided Gradle Wrapper by copying across
the following folder and files from the `Kotlin CorDapp Template <https://github.com/corda/cordapp-template-kotlin>`_ or the 
`Java CorDapp Template <https://github.com/corda/cordapp-template-kotlin>`_ to the root of your project:

* ``gradle/``
* ``gradlew``
* ``gradlew.bat``

Setting your dependencies
-------------------------

Choosing your Corda, Quasar and Kotlin versions
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
Several ``ext`` variables are used in a CorDapp's ``build.gradle`` file to define which versions are used to build your CorDapp:

* ``ext.corda_release_version`` defines the version of Corda
* ``ext.corda_gradle_plugins_version`` defines the version of the Corda Gradle Plugins
* ``ext.quasar_version`` defines the version of Quasar
* ``ext.kotlin_version`` defines the version of Kotlin (if using Kotlin to write your CorDapp)

``corda_gradle_plugins_versions`` are given in the form ``major.minor.patch``. You should use the same ``major`` and
``minor`` versions as the Corda version you are using, and the latest ``patch`` version. A list of all the available
versions can be found here: https://bintray.com/r3/corda/cordapp. If in doubt, you should base yourself on the version
numbers used in the ``build.gradle`` file of the
`Kotlin CorDapp Template <https://github.com/corda/cordapp-template-kotlin>`_ and the
`Java CorDapp Template <https://github.com/corda/cordapp-template-kotlin>`_.

For example, to use version 3.0 of Corda, version 3.0.8 of the Corda gradle plugins, version 0.7.9 of Quasar, and
version 1.1.60 of Kotlin, you'd write:

.. sourcecode:: groovy

    ext.corda_release_version = 'corda-3.0'
    ext.corda_gradle_plugins_version = '3.0.8'
    ext.quasar_version = '0.7.9'
    ext.kotlin_version = '1.1.60'

In certain cases, you may also wish to build against the unstable Master branch. See :doc:`building-against-master`.

Corda dependencies
^^^^^^^^^^^^^^^^^^
The ``cordformation`` plugin adds two new gradle configurations:

* ``cordaCompile``, which extends ``compile``
* ``cordaRuntime``, which extends ``runtime``

``cordaCompile`` and ``cordaRuntime`` indicate dependencies that should not be included in the CorDapp JAR. These
configurations should be used for any Corda dependency (e.g. ``corda-core``, ``corda-node``) in order to prevent a
dependency from being included twice (once in the CorDapp JAR and once in the Corda JARs).

Here are some guidelines for Corda dependencies:

* When building a CorDapp, you should always include ``net.corda:corda-core:$corda_release_version`` as a
  ``cordaCompile`` dependency, and ``net.corda:corda:$corda_release_version`` as a ``cordaRuntime`` dependency

* When building an RPC client that communicates with a node (e.g. a webserver), you should include
  ``net.corda:corda-rpc:$corda_release_version`` as a ``cordaCompile`` dependency

* When you need to use the network bootstrapper to bootstrap a local network (e.g. when using ``Cordformation``), you
  should include ``net.corda:corda-node-api:$corda_release_version`` as a ``cordaCompile`` dependency

* To use Corda's test frameworks, add ``net.corda:corda-test-utils:$corda_release_version`` as a ``testCompile``
  dependency. Never include ``corda-test-utils`` as a ``compile`` or ``cordaCompile`` dependency

* Any other Corda dependencies you need should be included as ``cordaCompile`` dependencies

Here is an overview of the various Corda dependencies:

* ``corda`` - The Corda fat JAR. Do not use as a compile dependency. Required as a ``cordaRuntime`` dependency when
  using ``Cordformation``
* ``corda-confidential-identities`` - A part of the core Corda libraries. Automatically pulled in by other libraries
* ``corda-core`` - Usually automatically included by another dependency, contains core Corda utilities, model, and
  functionality. Include manually if the utilities are useful or you are writing a library for Corda
* ``corda-core-deterministic`` - Used by the Corda node for deterministic contracts. Not likely to be used externally
* ``corda-djvm`` - Used by the Corda node for deterministic contracts. Not likely to be used externally
* ``corda-finance`` - The Corda finance CorDapp. Only include as a ``cordaCompile`` dependency if using as a dependent
  Cordapp or if you need access to the Corda finance types. Use as a ``cordapp`` dependency if using as a CorDapp
  dependency (see below)
* ``corda-jackson`` - Corda Jackson support. Use if you plan to serialise Corda objects to and/or from JSON
* ``corda-jfx`` - JavaFX utilities with some Corda-specific models and utilities. Only use with JavaFX apps
* ``corda-mock`` - A small library of useful mocks. Use if the classes are useful to you
* ``corda-node`` - The Corda node. Do not depend on. Used only by the Corda fat JAR and indirectly in testing
  frameworks
* ``corda-node-api`` - The node API. Required to bootstrap a local network
* ``corda-node-driver`` - Testing utility for programmatically starting nodes from JVM languages. Use for tests
* ``corda-notary-bft-smart`` - A Corda notary implementation
* ``corda-notary-raft`` - A Corda notary implementation
* ``corda-rpc`` - The Corda RPC client library. Used when writing an RPC client
* ``corda-serialization`` - The Corda core serialization library. Automatically included by other dependencies
* ``corda-serialization-deterministic`` - The Corda core serialization library. Automatically included by other
  dependencies
* ``corda-shell`` - Used by the Corda node. Never depend on directly
* ``corda-test-common`` - A common test library. Automatically included by other test libraries
* ``corda-test-utils`` - Used when writing tests against Corda/Cordapps
* ``corda-tools-explorer`` - The Node Explorer tool. Do not depend on
* ``corda-tools-network-bootstrapper`` - The Network Builder tool. Useful in build scripts
* ``corda-tools-shell-cli`` - The Shell CLI tool. Useful in build scripts
* ``corda-webserver-impl`` - The Corda webserver fat JAR. Deprecated. Usually only used by build scripts
* ``corda-websever`` - The Corda webserver library. Deprecated. Use a standard webserver library such as Spring instead

Dependencies on other CorDapps
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
You CorDapp may also depend on classes defined in another CorDapp, such as states, contracts and flows. There are two
ways to add another CorDapp as a dependency in your CorDapp's ``build.gradle`` file:

* ``cordapp project(":another-cordapp")`` (use this if the other CorDapp is defined in a module in the same project)
* ``cordapp "net.corda:another-cordapp:1.0"`` (use this otherwise)

The ``cordapp`` gradle configuration serves two purposes:

* When using the ``cordformation`` Gradle plugin, the ``cordapp`` configuration indicates that this JAR should be
  included on your node as a CorDapp
* When using the ``cordapp`` Gradle plugin, the ``cordapp`` configuration prevents the dependency from being included
  in the CorDapp JAR

Note that the ``cordformation`` and ``cordapp`` Gradle plugins can be used together.

Other dependencies
^^^^^^^^^^^^^^^^^^
If your CorDapps have any additional external dependencies, they can be specified like normal Kotlin/Java dependencies
in Gradle. See the example below, specifically the ``apache-commons`` include.

For further information about managing dependencies, see
`the Gradle docs <https://docs.gradle.org/current/userguide/dependency_management.html>`_.

.. _cordapp_build_system_signing_cordapp_jar_ref:

Signing the CorDapp JAR
^^^^^^^^^^^^^^^^^^^^^^^
The ``cordapp`` plugin can sign the generated CorDapp JAR file using `JAR signing and verification tool <https://docs.oracle.com/javase/tutorial/deployment/jar/signing.html>`_.
Signing the CorDapp enables its contract classes to use signature constraints instead of other types of the constraints,
for constraints explanation refer to :doc:`api-contract-constraints`.
By default the JAR file is signed by Corda development certificate.
The signing process can be disabled or configured to use an external keystore.
The ``signing`` entry may contain the following parameters:

 * ``enabled`` the control flag to enable signing process, by default is set to ``true``, set to ``false`` to disable signing
 * ``options`` any relevant parameters of `SignJar ANT task <https://ant.apache.org/manual/Tasks/signjar.html>`_,
   by default the JAR file is signed with Corda development key, the external keystore can be specified,
   the minimal list of required options is shown below, for other options referer to `SignJar task <https://ant.apache.org/manual/Tasks/signjar.html>`_:

   * ``keystore`` the path to the keystore file, by default *cordadevcakeys.jks* keystore is shipped with the plugin
   * ``alias`` the alias to sign under, the default value is *cordaintermediateca*
   * ``storepass`` the keystore password, the default value is *cordacadevpass*
   * ``keypass`` the private key password if it's different than the password for the keystore, the default value is *cordacadevkeypass*
   * ``storetype`` the keystore type, the default value is *JKS*

The parameters can be also set by system properties passed to Gradle build process.
The system properties should be named as the relevant option name prefixed with '*signing.*', e.g.
a value for ``alias`` can be taken from the ``signing.alias`` system property. The following system properties can be used:
``signing.enabled``, ``signing.keystore``, ``signing.alias``, ``signing.storepass``, ``signing.keypass``, ``signing.storetype``.
The resolution order of a configuration value is as follows: the signing process takes a value specified in the ``signing`` entry first,
the empty string *""* is also considered as the correct value.
If the option is not set, the relevant system property named *signing.option* is tried.
If the system property is not set then the value defaults to the configuration of the Corda development certificate.

The example ``cordapp`` plugin with plugin ``signing`` configuration:

.. sourcecode:: groovy

    cordapp {
        signing {
            enabled true
            options {
                keystore "/path/to/jarSignKeystore.p12"
                alias "cordapp-signer"
                storepass "secret1!"
                keypass "secret1!"
                storetype "PKCS12"
            }
        }
        //...

CorDapp auto-signing allows to use signature constraints for contracts from the CorDapp without need to create a
keystore and configure the ``cordapp`` plugin. For production deployment ensure to sign the CorDapp using your own
certificate e.g. by setting system properties to point to an external keystore or by disabling signing in ``cordapp``
plugin and signing the CordDapp JAR downstream in your build pipeline. CorDapp signed by Corda development certificate
is accepted by Corda node only when running in the development mode. In case CordDapp signed by the (default)
development key is run on node in the production mode (e.g. for testing), the node may be set to accept the development
key by adding the ``cordappSignerKeyFingerprintBlacklist = []`` property set to empty list (see
:ref:`Configuring a node <corda_configuration_file_signer_blacklist>`).

Signing options can be contextually overwritten by the relevant system properties as described above. This allows the
single ``build.gradle`` file to be used for a development build (defaulting to the Corda development keystore) and for
a production build (using an external keystore). The example system properties setup for the build process which
overrides signing options:

.. sourcecode:: shell

    ./gradlew -Dsigning.keystore="/path/to/keystore.jks" -Dsigning.alias="alias" -Dsigning.storepass="password" -Dsigning.keypass="password"

Without providing the system properties, the build will sign the CorDapp with the default Corda development keystore:

.. sourcecode:: shell

    ./gradlew

CorDapp signing can be disabled for a build:

.. sourcecode:: shell

    ./gradlew -Dsigning.enabled=false

Other system properties can be explicitly assigned to options by calling ``System.getProperty`` in ``cordapp`` plugin
configuration. For example the below configuration sets the specific signing algorithm when a system property is
available otherwise defaults to an empty string:

.. sourcecode:: groovy

    cordapp {
        signing {
            options {
                sigalg System.getProperty('custom.sigalg','')
            }
        }
        //...

Then the build process can set the value for *custom.sigalg* system property and other system properties recognized by
``cordapp`` plugin:

.. sourcecode:: shell

    ./gradlew -Dcustom.sigalg="SHA256withECDSA" -Dsigning.keystore="/path/to/keystore.jks" -Dsigning.alias="alias" -Dsigning.storepass="password" -Dsigning.keypass="password"

To check if CorDapp is signed use `JAR signing and verification tool <https://docs.oracle.com/javase/tutorial/deployment/jar/verify.html>`_:

.. sourcecode:: shell

   jarsigner --verify path/to/cordapp.jar

Cordformation plugin can also sign CorDapps JARs, when deploying set of nodes, see :doc:`generating-a-node`.

If your build system post-processes the Cordapp JAR, then the modified JAR content may be out-of-date or not complete
with regards to a signature file. In this case you can sign the Cordapp as a separate step and disable the automatic signing by the ``cordapp`` plugin.
The ``cordapp`` plugin contains a standalone task ``signJar`` which uses the same ``signing`` configuration.
The task has two parameters: ``inputJars`` - to pass JAR files to be signed
and an optional ``postfix`` which is added to the name of signed JARs (it defaults to "-signed").
The signed JARs are returned as  ``outputJars`` property.

For example in order to sign a JAR modified by *modifyCordapp* task,
create an instance of the ``net.corda.plugins.SignJar`` task (below named as *sign*).
The output of *modifyCordapp* task is passed to *inputJars* and the *sign* task is run after *modifyCordapp* one:

.. sourcecode:: groovy

    task sign(type: net.corda.plugins.SignJar) {
        inputJars modifyCordapp
    }
    modifyCordapp.finalizedBy sign
    cordapp {
        signing {
            enabled false
        }
        //..
    }

The task creates a new JAR file named *\*-signed.jar* which should be used further in your build/publishing process.
Also the best practice is to disable signing by the ``cordapp`` plugin as shown in the example.

Example
^^^^^^^
Below is a sample of what a CorDapp's Gradle dependencies block might look like. When building your own CorDapp, you
should base yourself on the ``build.gradle`` file of the
`Kotlin CorDapp Template <https://github.com/corda/cordapp-template-kotlin>`_ or the
`Java CorDapp Template <https://github.com/corda/cordapp-template-kotlin>`_.

.. container:: codeset

    .. sourcecode:: groovy

        dependencies {
            // Corda integration dependencies
            cordaCompile "net.corda:corda-core:$corda_release_version"
            cordaCompile "net.corda:corda-finance:$corda_release_version"
            cordaCompile "net.corda:corda-jackson:$corda_release_version"
            cordaCompile "net.corda:corda-rpc:$corda_release_version"
            cordaCompile "net.corda:corda-node-api:$corda_release_version"
            cordaCompile "net.corda:corda-webserver-impl:$corda_release_version"
            cordaRuntime "net.corda:corda:$corda_release_version"
            cordaRuntime "net.corda:corda-webserver:$corda_release_version"
            testCompile "net.corda:corda-test-utils:$corda_release_version"

            // Corda Plugins: dependent flows and services
            // Identifying a CorDapp by its module in the same project.
            cordapp project(":cordapp-contracts-states")
            // Identifying a CorDapp by its fully-qualified name.
            cordapp "net.corda:bank-of-corda-demo:1.0"

            // Some other dependencies
            compile "org.jetbrains.kotlin:kotlin-stdlib-jre8:$kotlin_version"
            testCompile "org.jetbrains.kotlin:kotlin-test:$kotlin_version"
            testCompile "junit:junit:$junit_version"

            compile "org.apache.commons:commons-lang3:3.6"
        }

Creating the CorDapp JAR
------------------------
Once your dependencies are set correctly, you can build your CorDapp JAR(s) using the Gradle ``jar`` task

* Unix/Mac OSX: ``./gradlew jar``

* Windows: ``gradlew.bat jar``

Each of the project's modules will be compiled into its own CorDapp JAR. You can find these CorDapp JARs in the ``build/libs`` 
folders of each of the project's modules.

.. warning:: The hash of the generated CorDapp JAR is not deterministic, as it depends on variables such as the
   timestamp at creation. Nodes running the same CorDapp must therefore ensure they are using the exact same CorDapp
   JAR, and not different versions of the JAR created from identical sources.

The filename of the JAR must include a unique identifier to deduplicate it from other releases of the same CorDapp.
This is typically done by appending the version string to the CorDapp's name. This unique identifier should not change
once the JAR has been deployed on a node. If it does, make sure no one is relying on ``FlowContext.appName`` in their
flows (see :doc:`versioning`).

Installing the CorDapp JAR
--------------------------

.. note:: Before installing a CorDapp, you must create one or more nodes to install it on. For instructions, please see
   :doc:`generating-a-node`.

At start-up, nodes will load any CorDapps present in their ``cordapps`` folder. In order to install a CorDapp on a node, the 
CorDapp JAR must be added to the ``<node_dir>/cordapps/`` folder (where ``node_dir`` is the folder in which the node's JAR 
and configuration files are stored) and the node restarted.

CorDapp configuration files
---------------------------

CorDapp configuration files should be placed in ``<node_dir>/cordapps/config``. The name of the file should match the
name of the JAR of the CorDapp (eg; if your CorDapp is called ``hello-0.1.jar`` the config should be ``config/hello-0.1.conf``).

Config files are currently only available in the `Typesafe/Lightbend <https://github.com/lightbend/config>`_ config format.
These files are loaded when a CorDapp context is created and so can change during runtime.

CorDapp configuration can be accessed from ``CordappContext::config`` whenever a ``CordappContext`` is available.

There is an example project that demonstrates in ``samples`` called ``cordapp-configuration`` and API documentation in
`<api/kotlin/corda/net.corda.core.cordapp/index.html>`_.


Minimum and target platform version
-----------------------------------

CorDapps can advertise their minimum and target platform version. The minimum platform version indicates that a node has to run at least this version in order to be able to run this CorDapp. The target platform version indicates that a CorDapp was tested with this version of the Corda Platform and should be run at this API level if possible. It provides a means of maintaining behavioural compatibility for the cases where the platform's behaviour has changed. These attributes are specified in the JAR manifest of the CorDapp, for example:

.. sourcecode:: groovy

    'Min-Platform-Version': 4
    'Target-Platform-Version': 4

Using the `cordapp` Gradle plugin, this can be achieved by putting this in your CorDapp's `build.gradle`:

.. container:: codeset

    .. sourcecode:: groovy

        cordapp {
            info {
                targetPlatformVersion 4
                minimumPlatformVersion 4
            }
        }

Without using the `cordapp` plugin, you can achieve the same by modifying the jar task as shown in this example:

.. container:: codeset

    .. sourcecode:: groovy

        jar {
            manifest {
                attributes(
                        'Min-Platform-Version': 4
                        'Target-Platform-Version': 4
                )
            }
        }
