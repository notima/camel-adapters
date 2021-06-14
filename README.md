# camel-adapters
Apache Camel adapters for various formats and systems

## Installation

In karaf shell

```
feature:repo-add mvn:org.notima.camel-adapters/camel-feature/LATEST/xml/features
feature:install camel-fortnox4j
feature:install camel-sveawebpay
feature:install camel-ubl
feature:install camel-infometric
```

### Snapshots

To use snapshots, make sure that ``${KARAF_HOME}/etc/org.ops4j.pax.url.mvn.cfg`` has the repository 

     https://oss.sonatype.org/content/repositories/snapshots@id=mvncentral.snaps@snapshots
     
included in the ``org.ops4j.pax.url.mvn.repositories`` property.
