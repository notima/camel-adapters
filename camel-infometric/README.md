# camel-infometric

Infometric Adapter that reads a billing file.

Example usage in Camel

```

<?xml version="1.0" encoding="UTF-8"?>
<blueprint xmlns="http://www.osgi.org/xmlns/blueprint/v1.0.0"
           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xmlns:cm="http://aries.apache.org/blueprint/xmlns/blueprint-cm/v1.1.0"
           xsi:schemaLocation="
           		http://www.osgi.org/xmlns/blueprint/v1.0.0 https://www.osgi.org/xmlns/blueprint/v1.0.0/blueprint.xsd">

	<camelContext id="MyInfometricRoutes" xmlns="http://camel.apache.org/schema/blueprint">

		<dataFormats>
			<jaxb id="bo" contextPath="org.notima.generic.businessobjects"/>
		</dataFormats>

		<route id="ReadInfometricCSVFile">

			<from uri="file:///home/user/infometric"/>
			
			<setHeader headerName="productKey">
				<constant>105</constant>
			</setHeader>
			
			<setHeader headerName="unitPrice">
				<constant>1.45</constant>
			</setHeader>
			
			<setHeader headerName="invoiceLineText">
				<constant>Elförbrukning</constant>
			</setHeader>

			<to uri="direct-vm:splitInfometricCSVFile" />

			<marshal ref="bo" />

			<to uri="file:///tmp" />

		</route>


	</camelContext>

</blueprint>


```

