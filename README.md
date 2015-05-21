# Elasticsearch Report Plugin
##Elasticsearch Report Plugin to Generate Excel Report

There are a lot of companies(small scale to large scale companies) who use Elasticsearch to store massive amount of to data.Most of them find it hard to generate simple reports from Elasticsearch to get information out of it.  So this Elasticsearch plugin can be used to generate reports(Excel) with simple JSON input. This plugin can save the report to the server and also can send E-Mail to the configured recipients. We can also perform some basic operations on the fields to get a computed field in the Excel report.

####List of Operation : 

1. getValue -  Get the Elasticsearch field value
2. getDValue - Get custom value based on the Elasticsarch field value
3. Length - Get the length of Elasticsarch field value
4. Format Number Length - Format integer to given length
5. Sub String - Get substring of Elasticsarch field value
6. Character at index - Get the character at given index of Elasticsarch field value
7. Calculate - Perform arithmetic operation on Elasticsarch field values
8. Range - Get custom value based on range condition of Elasticsarch field value
9. Array indexOf(int value) - Get index of given integer value from an Elasticsarch array
10. Array indexOf(String value) - Get index of given string value from an Elasticsearch array 
11. Array valueAt(index) - Get the value of given index from an Elasticsarch array


####Operation Syntax and Object Type : 

| # | Operation | Syntax | Object Type |
|---|-----------|---------|------------|
|1|getValue|[0,ES_FIELD]|[0,String]|
|2|getDValue |[1,valueMappingKey,VALUE]|[1,String,String]|
|3|Length |[2,VALUE]|[2,String]|
|4|Format Number Length|[3,VALUE,FORMAT_LENGTH]|[3,String,int]|
|5|Sub String|[4,VALUE,from,to]|[4,String,int,int]|
|6|Character at index |[5,VALUE,index]|[5,String,int]|
|7|Calculate |[6, ARITHMETIC_EXPRESSION]|[6,String]|
|8|Range |[7,valueMappingKey,VALUE]|[7,String,String]|
|9|Array indexOf(int value) |[8,ES_ARRAY_FIELD, value]|[8,String,int]|
|10|Array indexOf(String value)|[9,ES_ARRAY_FIELD,value]|[9,String,String]|
|11|Array valueAt(index) |[10,VALUE,index]|[10,String,int]|

#### Syntax Description :
|Operation | Description|
|---|-----------|
|ES_FIELD| A field in a Elasticsearch document|
|ES_ARRAY_FIELD| A field in a Elasticsearch document|
|valueMappingKey| A field in input JSON given by user |
|ARITHMETIC_EXPRESSION| Any string representing conditional or arithmetic expression |
|VALUE| Can be either string given by user or any other OPERATION. i.e. we can nest the above operations to whatever level we want. In other words, we can perform multi-level OPERATION on the Elasticsearch fields |


####Detailed Example with Sample Data :

#####Elasticsearch Details :

Consider we have following data in Elasticsearch.

<table>
<tr>
<td>Index</td><td>student</td>
</tr>
<tr>
<td>Type</td><td>details</td>
</tr>
</table>

__Sample Input Data__
<pre>
{
   "name":"Ramu",
   "gender":"Male",
   "serial_number": "C1112"
   "marks":{
      "maths":78,
      "english":80,
      "economics":75
      },
   "marksArrayInt":[78,80,75]
   "marksArrayString":["78","80","75"]
}
</pre>

__Sample Input for Plugin__
<pre>
{
	"reportTitle":"Student Details Report",
	"reportName": "Student Details",
	"reportAccess": {
		"fileName": "Student Details Report",
		"ftp": {
			"filePath": "F:\\tmp\\"
		},
		"email": {
			"subject": "Student Report - Detailed Report",
			"deliverTo": [ "E-Mail-ID" ]
		}
	},
	"description": "The attachment contains detailed report of students.",
	"batchSize": 250,
	"host":"localhost",
	"index":"student",
	"type":"details",
	"routing":"",
	"valueMappingKey":{
	   "genderMappingKey":{
         "Male":"M",
         "Female":"F"
      },
      "isPassMappingKey":{
         "x>=40":"PASS",
         "x<40":"FAIL"
      }
	}
	"statement": {
      "query": {
         "match_all": {
         }
      },
      "fields":["name","gender","serial_number","marks.maths","marks.english","marks.economics","","","","","","","","","","",]
   },
	"config": [
      {
		"title": "Name",
		"format":"[0,name]"
	   },
	   {
		"title": "Gender",
		"format":"[0,gender]"
	   },
	   {
		"title": "Serial Number",
		"format":"[0,serial_number]"
	   },
	   {
		"title": "Maths",
		"format":"[0,marks.maths]"
	   },
	   {
		"title": "English",
		"format":"[0,marks.english]"
	   },
	   {
		"title": "Economics",
		"format":"[0,marks.economics]"
	   },
	   {
		"title": "",
		"format":"[0,]"
	   },
	   {
		"title": "",
		"format":"[0,]"
	   },
	   {
		"title": "",
		"format":"[0,]"
	   },
	   {
		"title": "",
		"format":"[0,]"
	   },
	   {
		"title": "",
		"format":"[0,]"
	   },
	   {
		"title": "",
		"format":"[0,]"
	   },
	   {
		"title": "",
		"format":"[0,]"
	   },
	   {
		"title": "",
		"format":"[0,]"
	   },
	   {
		"title": "",
		"format":"[0,]"
	   },
	   {
		"title": "",
		"format":"[0,]"
	   }
	]
}
</pre>



####Operation and Example :
| # | Operation | Example | Output|
|---|-----------|---------|---------|
|1|getValue|[0,name]<br>[0,gender]<br>[0,marks.maths]|Ramu<br>Male<br>78|
|2|getDValue |[1,genderMappingKey,Male]|M|
|3|Length |[2,Ramu]|4|
|4|Format Number Length|[3,632,5]|00632|
|5|Sub String|[4,Ramu,1,3]|amu|
|6|Character at index |[5,Ramu,2]|m|
|7|Calculate |[6, (2+3)*7]|35|
|8|Range |[7,isPassMappingKey,85]|PASS|
|9|Array indexOf(int value) |[8,marksArrayInt, 80]|1|
|10|Array indexOf(String value)|[9,marksArrayString,80]|1|
|11|Array valueAt(index) |[10,marksArrayInt,2]|75|



