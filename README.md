# camel-adapters
Apache Camel adapters for various formats and systems.

Depends on https://github.com/notima/businessObjectAdapters

## Installation in Karaf

In karaf shell

```
feature:repo-add mvn:org.notima.camel-adapters/camel-feature/0.0.8/xml/features
feature:install camel-fortnox4j
feature:install camel-sveawebpay
feature:install camel-ubl
feature:install camel-infometric
```

## Make a redeployable kar in Karaf

	kar:create camel-notima-utils-0.0.8

### Snapshots

To use snapshots, make sure that ``${KARAF_HOME}/etc/org.ops4j.pax.url.mvn.cfg`` has the repository 

     https://oss.sonatype.org/content/repositories/snapshots@id=mvncentral.snaps@snapshots
     
included in the ``org.ops4j.pax.url.mvn.repositories`` property.
