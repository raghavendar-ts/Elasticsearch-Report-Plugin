# Elasticsearch Report Plugin
##Elasticsearch Report Plugin to Generate Excel Report

There are a lot of companies(small scale to large scale companies) who use Elasticsearch to store massive amount of to data.Most of them find it hard to generate simple reports from Elasticsearch to get information out of it.  So this Elasticsearch plugin can be used to generate reports(Excel) with simple JSON input. This plugin can save the report to the server and also can send E-Mail to the configured recipients. We can also perform some basic operations on the fields to get a computed field in the Excel report.

####Installing and Removing the Plugin : 
Go to ES_HOME/bin

__Command to Install :__
<pre>
 plugin --install esreport --url https://github.com/raghavendar-ts/Elasticsearch-Report-Plugin/blob/master/target/releases/es-report-plugin-1.0-SNAPSHOT.zip?raw=true
</pre>
__Command to Remove :__
<pre>
plugin --remove esreport
</pre>

__Configure E-Mail Properties :__
<ol>
	<li>Once you have installed the plugin, go to <b>ES_HOME/plugins/esreport/properties</b>.</li>
	<li>Open the file named <b>mail.properties</b>.</li>
	<li>Give a valid username and password with the mail server properties (The existing properties can be used for G-Mail account).</li>
	<li>Note :
		<ol>
			<li>Google (G-Mail) by default will not allow any third party applications to access the G-Mail account programmatically. But Google provides an option to turn it on. To enable the option, go to <b>Less secure apps (https://www.google.com/settings/security/lesssecureapps)</b> and <b>Turn On</b> the option for <b>Access for less secure apps</b>. Once this option is turned on, the Elasticsearch Report Plugin can access the G-Mail account using the credentials given in <b>mail.properties</b> to send the generated report as mail attachment.</li>
			<li>Make sure to disable any anti-virus program running in your machine since it may block the outgoing mail requests sent by third party applications. In my case Avast was blocking the outgoing mail requests.</li>
			<li>You can also make the plugin to store the reports in the server where elasticsearch is running or to any folder shared using the network.</li>
		</ol>
	</li>
</ol>
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
|VALUE| Can be either string given by user or any other OPERATION. i.e. we can nest the above operations. In other words, we can perform multi-level nested OPERATION on the Elasticsearch fields |


####Detailed Example with Sample Data :

__Sample Input Data :__
<pre>
{
   "name":"Ramu",
   "gender":"Male",
   "register_number": "5723",
   "marks":{
      "computer_science":78,
      "data_mining":80,
      "dbms":75
      },
   "marksArrayInt":[78,80,75],
   "marksArrayString":["78","80","75"]
}
</pre>

__Sample HTTP Request to the Plugin :__ 
<pre>
POST /_river/report/now
{
	"reportTitle": "Student Details Report",
	"reportName": "Student Details",
	"reportAccess": {
		"fileName": "Student Details Report",
		"ftp": {
			"filePath": "F:\\properties\\"
		},
		"email": {
			"subject": "Student Report - Detailed Report",
			"description": "The attachment contains detailed report of students and their marks.",
			"deliverTo": ["E-Mail ID 1",
			"E-Mail ID 2"]
		}
	},
	"batchSize": 250,
	"index": "student",
	"type": "details",
	"valueMapping": {
		"genderMappingKey": {
			"Male": "M",
			"Female": "F"
		},
		"isPassMappingKey": {
			"x>=40": "PASS",
			"x<40": "FAIL"
		},
		"courseList": {
			"5": "Bachelor of Technology (B.Tech)",
			"6": "Master of Technology (M.Tech)",
			"default": "NA"
		},
		"branchList": {
			"7": "Computer Science",
			"8": "Information Technology"
		}
	},
	"statement": {
		"query": {
			"match_all": {
				
			}
		},
		"fields": ["name",
		"gender",
		"register_number",
		"marks.computer_science",
		"marks.data_mining",
		"marks.dbms",
		"marksArrayInt",
		"marksArrayString"]
	},
	"config": [{
		"title": "Name",
		"format": "[0,name]"
	},
	{
		"title": "Name (String Length)",
		"format": "[2,[0,name]]"
	},
	{
		"title": "Gender",
		"format": "[0,gender]"
	},
	{
		"title": "Register Number",
		"format": "[0,register_number]"
	},
	{
		"title": "Computer Science",
		"format": "[0,marks.computer_science]"
	},
	{
		"title": "English",
		"format": "[0,marks.data_mining]"
	},
	{
		"title": "Economics",
		"format": "[0,marks.dbms]"
	},
	{
		"title": "Course Code",
		"format": "[5,[0,register_number],0]"
	},
	{
		"title": "Course",
		"format": "[1,courseList,[5,[0,register_number],0]]"
	},
	{
		"title": "Branch Code",
		"format": "[5,[0,register_number],1]"
	},
	{
		"title": "Branch",
		"format": "[1,branchList,[5,[0,register_number],1]]"
	},
	{
		"title": "Roll No",
		"format": "[4,[0,register_number],2,3]"
	},
	{
		"title": "Total Marks",
		"format": "[6,[0,marks.computer_science]+[0,marks.data_mining]+[0,marks.dbms]]"
	},
	{
		"title": "Average",
		"format": "[6,([0,marks.computer_science]+[0,marks.data_mining]+[0,marks.dbms])/3]"
	},
	{
		"title": "Pass/Fail",
		"format": "[7,isPassMappingKey,[6,[0,marks.computer_science]+[0,marks.data_mining]+[0,marks.dbms]/3]]"
	}]
}
</pre>

####Operation and Example :


<table>
	<tr>
		<th>#</th><th>Operation</th><th>Column</th><th>Example</th><th>Output</th>
	</tr>
	<tr>
		<td>1</td>
		<td>getValue</td>
		<td>
			<ol>
				<li>Name</li>
				<li>Gender</li>
				<li>Register Number</li>
				<li>Mark in Computer Science</li>
				<li>Mark in Data Mining</li>
				<li>Mark in DBMS</li>
			</ol>
		</td>
		<td>
			<ol>
				<li>[0,name]</li>
				<li>[0,gender]</li>
				<li>[0,register_number]</li>
				<li>[0,marks.computer_science]</li>
				<li>[0,marks.data_mining]</li>
				<li>[0,marks.dbms]</li>	
			</ol>
		</td>		
		<td>
			<ol>
				<li>Ramu</li>
				<li>Male</li>
				<li>5723</li>
				<li>78</li>
				<li>80</li>
				<li>75</li>	
			</ol>
		</td>
	</tr>
	<tr>
		<td>2</td>
		<td>getDerivedValue</td>
		<td>
			<ol>
				<li>Gender (Short Form)</li>
				<li>Course</li>
				<li>Branch</li>
		</td>
		<td>
			<ol>
				<li>[1,genderMappingKey,Male]</li>
				<li>[1,courseList,[5,register_number,0]]</li>
				<li>[1,branchList,[5,register_number,1]]</li>	
			</ol>
		</td>
		<td>
			<ol>
				<li>M</li>
				<li>Bachelor of Technology (B.Tech)</li>
				<li>Computer Science</li>
			</ol>
		</td>
	</tr>
	<tr>
		<td>3</td>
		<td>Length</td>
		<td>Name </td>
		<td>[2,Ramu]</td>
		<td>4</td>
	</tr>
	<tr>
		<td>4</td>
		<td>Format Number Length</td>
		<td>-</td>
		<td>[3,632,5]</td>
		<td>00632</td>
	</tr>
	<tr>
		<td>5</td>
		<td>Sub String</td>
		<td>Roll No</td>
		<td>[4,[0,register_number],2,3] </td>
		<td>23</td>
	</tr>
	<tr>
		<td>6</td>
		<td>Character at index</td>
		<td>
		<ol>
			<li>Course Code</li>
			<li>Branch Code</li>
		</ol>	
		</td>
		<td>
			<ol>
				<li>[5,register_number,0]</li>
				<li>[5,register_number,1]</li>				
			</ol>
		</td>
		<td>
			<ol>
				<li>5</li>
				<li>7</li>				
			</ol>
		</td>
	</tr>
	<tr>
		<td>7</td>
		<td>Calculate</td>
		<td>
			<ol>	
				<li>Total</li>
				<li>Average</li>				
			</ol>
		</td>
		<td> 
			<ol>	
				<li>[6,[0,marks.computer_science]+[0,marks.data_mining]+[0,marks.dbms]]</li>
				<li>[6,[0,marks.computer_science]+[0,marks.data_mining]+[0,marks.dbms]/3]</li>				
			</ol>
		</td>
		<td>
			<ol>	
				<li>233</li>
				<li>77.66</li>				
			</ol>
		</td>
	</tr>
	<tr>
		<td>8</td>
		<td>Range</td>
		<td>Pass/Fail</td>
		<td>
			<ol>	
				<li>[7,isPassMappingKey,85]</li>
				<li>[7,isPassMappingKey,35]</li>				
			</ol>
		</td>
		<td>
			<ol>	
				<li>Pass</li>
				<li>Fail</li>				
			</ol>
		</td>
	</tr>
	<tr>
		<td>9</td>
		<td>Array indexOf(int value)</td>
		<td>
			<ol>	
				<li>Index of Value 80</li>
				<li>Index of value 78</li>				
			</ol>
		</td>
		<td>
			<ol>	
				<li>[8,marksArrayInt, 80]</li>
				<li>[8,marksArrayInt, 78]</li>				
			</ol>
		</td>
		<td>
			<ol>	
				<li>1</li>
				<li>0</li>				
			</ol>
		</td>
	</tr>
	<tr>
		<td>10</td>
		<td>Array indexOf(String value)</td>
		<td>
			<ol>	
				<li>Index of value 80</li>
				<li>Index of value 75</li>				
			</ol>
		</td>
		<td>
			<ol>	
				<li>[9,marksArrayString,80]</li>
				<li>[9,marksArrayString,75]</li>				
			</ol>
		</td>
		<td>
			<ol>	
				<li>1</li>
				<li>2</li>				
			</ol>
		</td>
	</tr>
	<tr>
		<td>11</td>
		<td>Array valueAt(index)</td>
		<td>
			<ol>	
				<li>Value at index 2</li>
				<li>Value at index 0</li>				
			</ol>
		</td>
		<td>
			<ol>	
				<li>[10,marksArrayInt,2]</li>
				<li>[10,marksArrayInt,0]</li>				
			</ol>
		</td>
		<td>
			<ol>	
				<li>75</li>
				<li>78</li>				
			</ol>
		</td>
	</tr>
</table>


####Sample Output :

__Download Excel :__[Link](https://github.com/raghavendar-ts/Elasticsearch-Report-Plugin/blob/master/output/Student%20Details%20Report_20150523_1823.xls?raw=true)__

