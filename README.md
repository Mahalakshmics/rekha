## Environment:
- Java version: 1.8
- Maven version: 3.*
- Spring Boot version: 2.2.1.RELEASE

## Data:
XML Input:
```
<?xml version="1.0"?>
<AMHMessage xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
<Header>
</Header>
    <Data>
        <Direction>O</Direction>
		<TxnAmount>100<TxnAmount>
        <UniqueID>210428PN100067340</UniqueID>
        <TrafficType>REQ</TrafficType>
        <Country>ph</Country>
        <MsgType>fin.103.2020</MsgType>
        <Service>swift.fin</Service>
        <Sender>SCBLPHM0XXX</Sender>
        <Receiver>SCBLPHM0XXX</Receiver>
        <ApplicationID>51041-</ApplicationID>
        <DataOwner>PHCASH</DataOwner>
        <JMStopic>v21/51338-amh/o/fin.103.2020/SA/req/0/51041-</JMStopic>
	</Data>
    <Data>
        <Direction>O</Direction>
		<TxnAmount>100<TxnAmount>
        <UniqueID>210428PN100067340</UniqueID>
        <TrafficType>REQ</TrafficType>
        <Country>ph</Country>
        <MsgType>fin.103.2020</MsgType>
        <Service>swift.fin</Service>
        <Sender>SCBLPHM0XXX</Sender>
        <Receiver>SCBLPHM0XXX</Receiver>
        <ApplicationID>51041--</ApplicationID>
        <DataOwner>PHCASH</DataOwner>
        <JMStopic>v21/51338-amh/o/fin.103.2020/IN/req/0/51041-</JMStopic>
	</Data>	
</AMHMessage> 

```

Output JSON:

TotAmt and TotCnt should be calculated and both should be populated in header.
validate as UniqueID has only alphanumeric and no special characters, If it has any, throw error and stop the process and revert.
```
AMHMessage{
Header{
"TotAmt":"200",
"TotCnt":"2"
}
Data[{
"Direction":"O",
"TxnAmount":"100",
"UniqueID":"210428PN100067340",
"TrafficType":"REQ",
"Country":"ph",
"MsgType":"fin.103.2020",
"Service":"swift.fin",
"Sender":"SCBLPHM0XXX",
"Receiver":"SCBLPHM0XXX",
"ApplicationID":"51041-",
"DataOwner":"PHCASH",
"JMStopic":"v21/51338-amh/o/fin.103.2020/SA/req/0/51041-",
"BaseCountry":"SA"
},
{"Direction":"O",
"TxnAmount":"100",
"UniqueID":"210428PN100067340",
"TrafficType":"REQ",
"Country":"ph",
"MsgType":"fin.103.2020",
"Service":"swift.fin",
"Sender":"SCBLPHM0XXX",
"Receiver":"SCBLPHM0XXX",
"ApplicationID":"51041-scpay-",
"DataOwner":"PHCASH",
"JMStopic":"v21/51338-amh/o/fin.103.2020/IN/req/0/51041-scpay-"
"BaseCountry":"IN",
}
]
}

```
Input String:

04162FHDREFTOF100000466YN5759563600000000ABCDEFGHIJKL0000000424NBJAZBSFRSBCPAY102   50BJAZHS1192750001                NTN\n:21:Ref512\n:50K:/1234567890\nABC CUSTOMER\nBUILDING LANE\nSA\n:20:MyRef\n:23:CRED\n:71A:SHA\n:59:/1234567890\nPQR CUSTOMER\nRIYADH\nSAUDI ARABIA\n:21:Ref512\n:50F:CUST/UK/ABC BANK/121231234342\n1/Sherlock Holmes\n1/Sherlock\n2/PENNIYAR STREET\n2/PENNIYAR\n2/ABC\n3/UK\n3/STREET\n4/09122018\n6/UK/BANK/1234\n7/CUST/UK\n:59F:/12345\n1/ABN\n1/BNN\n3/ABC STREET\n3/ANC

Output:

1) In 59F:
	- 1/ ->Map the text following "1/" to /data[] /cdtr /nm , look for repetition in the next line , repetition to be concatenated.
	- 2/ ->Map the whole text into  /data[] /cdtr /pstlAdr /adrLine[] , along with the "2/" value , look for repetition in the next line , repetition to be concatenated.
	- 3/ -> Map the whole text into  /data[] /cdtr /pstlAdr /adrLine[] , along with the "3/" value , look for repetition in the next line , repetition to be concatenated
	
2) In 50F:
	- Line 1 - "CUST" as a value -> 
Split values using delimiter "/"
Map the first value to /data[] /dbtr /prvtId /othr[] /idSchmeNmCd
Concatenate the second and the third value separted by "/" 
Map the concatenated value to /data[] /dbtr /prvtId /othr[] /idIssr 
Map the next value(last value ) to /data[] /dbtr /prvtId /othr[] /id

Ex(O1) 50F:CUST/UK/ABC BANK/123456789/8-123456
Example(O1)- 
/data[] /dbtr /prvtId /othr[] /idSchmeNmCd =CUST
/data[] /dbtr /prvtId /othr[] /idIssr = UK/ABC BANK
/data[] /dbtr /prvtId /othr[] /id = 123456789/8-123456

	- Line 2 ->
	-1/ -> map the text following "1/" to /data[] /dbtr /nm , look for repetition in the next line , repetition to be concatenated
	-2/ -> Map the whole text into /data[] /dbtr /pstlAdr /adrLine[] along with the "2/" value , look for repetition in the next line , repetition to be concatenated
	-3/ -> Map the whole text into /data[] /dbtr /pstlAdr /adrLine[] along with the "3/" value , look for repetition in the next line , repetition to be concatenated
	-4/ -> Map to tag following "4/" to /data[] /dbtr /prvtId /birthDt
	-6/ -> Split values using delimiter "/"
			Map the "6/" value to /data[] /dbtr /prvtId /othr[] /idSchmeNmCd
			Concatenate the first and the second value separted by "/" 
			Map the concatenated value to /data[] /dbtr /prvtId /othr[] /idIssr 
			Map the next value(last value ) to /data[] /dbtr /prvtId /othr[] /id
	-7/ -> Split values using delimiter "/"
			Map the "7/" value to /data[] /dbtr /prvtId /othr[] /idSchmeNmCd
			Map the first value to /data[] /dbtr /prvtId /othr[] /idIssr
			Map the next value(last value )to /data[] /dbtr /prvtId /othr[] /id		


## Requirements:
1) The task is to write a common framework which accepts above XML/String as a input and converted into expected JSON.
2) Add on to that, This framework should be able to work for any different XML/String as a input with some configuration change.
The configuration changes should not be in code and requires rebuilding the application.

####API's

**POST** request to `/api/auth`:

- implement role based username password authentication
- return JWT token on valid authentication


**POST** request to `/api/convert`:

- Accepts input as a XML/String.
- output should be JSON in a download format.

#### Instructions
- Users should be authenticated before any operations. (Role based authentication, for ex: Only admin users can create/delete the account)

#####Deliverable
-	Completely working solution with Java source code
-	Production ready code (Integration and unit tests are mandatory)
-	Fully functional API’s.
-	All API’s with rate limiting.

## Commands
- run: 
```bash
mvn clean package; java -jar target/stocktrades-1.0.jar
```
- install: 
```bash
mvn clean install
```
- test: 
```bash
mvn clean test
```
