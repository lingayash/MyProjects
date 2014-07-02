<!DOCTYPE html>
<html lang = "en">
<head>
	<title>ZIPCODE LOCATOR</title>
	<meta charset="utf-8">
	<meta name="description" content="ZIPCODE">
	<meta name="keywords" content="ZIPCODE">
	<link rel="stylesheet" href="HW4.css">
	<script type="text/javascript">
		var tempLat, tempLon;
	</script>
</head>
<body>
	<div class="wrapper">
		<div class="otherMain">
			<div class="title">
				<div id="topInfo">
					<span id="heading">THE COM214 ZIP CODE LOCATOR</span>
					<form action="hw4.php" method="get">
						<input type="submit" name="button" class="topButtons" value="Create DB" />
						<input type="submit" name="button" class="topButtons" value="Delete DB" />
					</form>
				</div>
			</div>
		</div>
		<div class="canMain">
			<canvas id="myCanvas" width="935" height="495" >
                Your browser does not support the canvas element.
            </canvas>
		</div>
		<div class="otherMain">
			<div class="title">
				<form action="hw4.php" method="get">
					<div id="info">
						LATITUDE:<input type="double" id="ypos" name="lat1" value="<?php echo isset($_GET['lat1']) ? $_GET['lat1'] : '' ?>" readonly>
						LONGITUDE:<input type="double" id="xpos" name="lon1" value="<?php echo isset($_GET['lon1']) ? $_GET['lon1'] : '' ?>" readonly> 
						<input type="submit" class="buttons" name="button" value="List Nearby Zip Codes"  />
						<span>Items per page</span>
						<select id = "selectItems" name="items">
							<option value = 5>5</option>
							<option value = 10>10</option>
							<option value = 15>15</option>
							<option value = 20>20</option>
						</select>
						<?php
							if(isset($_GET["items"])){
							echo "<script type=\"text/javascript\">".PHP_EOL;
							echo "document.getElementById('selectItems').value=".$_GET['items'].";".PHP_EOL;
							echo "tempLat=".$_GET['lat1'].";".PHP_EOL;
							echo "tempLon=".$_GET['lon1'].";".PHP_EOL;
						//	echo "console.log(tempLat,tempLon);".PHP_EOL;
							echo"</script>".PHP_EOL;
							}
						?>
					</div>
				</form>
			</div>
			<div id="dummy">
			</div>
		</div>
	</div>
	<?php
		function createDB(){
			$db_conn = mysql_connect("localhost", "root", "");
			if (!$db_conn)
				die("Unable to connect: " . mysql_error()); 
			mysql_query("CREATE DATABASE smartdb", $db_conn);
			mysql_select_db("smartdb", $db_conn);
			$cmd = "CREATE TABLE zcodes (
				  zipcode int(5) NOT NULL PRIMARY KEY,
				  city varchar(30),
				  state varchar(3),
				  lat double,
				  lon double,
				  timezone int,
				  dist int
				)";
			mysql_query($cmd);
			$cmd = "LOAD DATA LOCAL INFILE 'zip_codes_usa.csv' INTO TABLE zcodes
					FIELDS TERMINATED BY ','";
			mysql_query($cmd);
			mysql_close($db_conn);
		}
		function deleteDB(){
			$db_conn = mysql_connect("localhost", "root", "");
			if (!$db_conn)
				die("Unable to connect: " . mysql_error());
			mysql_query("DROP DATABASE smartdb", $db_conn );
			sleep(1);
			mysql_close($db_conn);
		}
		
		function latLonToMiles($lat1, $lon1, $lat2, $lon2){  //haversine formula
			$R = 3961;  // radius of the Earth in miles
			$dlon = ($lon2 - $lon1)*M_PI/180;
			$dlat = ($lat2 - $lat1)*M_PI/180;
			$lat1 *= M_PI/180;
			$lat2 *= M_PI/180;
			$a = pow(sin($dlat/2),2) + cos($lat1) * cos($lat2) * pow(sin($dlon/2),2) ;
			$c = 2 * atan2( sqrt($a), sqrt(1-$a) ) ;
			$d = $R * $c;
			return $d;	
		}
		function listCodes(){
			createDB();
			$db_conn = mysql_connect("localhost", "root", "");
			mysql_select_db("smartdb", $db_conn);
			$lat1 = $_GET["lat1"];
			$lon1 = $_GET["lon1"];
			$items = $_GET["items"];
			$cmd = "SELECT *,
						SQRT(POW(($lat1-lat),2)+POW(($lon1-lon),2)) as dist
					FROM zcodes ORDER BY dist ASC limit $items ";
			
			$zlist = mysql_query($cmd);
			echo "<script type=\"text/javascript\">" . PHP_EOL;
			echo "var txt=\"\";" . PHP_EOL;
			echo "txt+='<div id=\"listHolder\">';" . PHP_EOL;
			echo "txt+='<table>';" . PHP_EOL;
			echo "txt+='<tr><th>Zip Code</th><th>City</th><th>State</th><th>Latitude</th><th>Longitude</th><th>Distance(miles)</th><th>Time Difference(ET)</th></tr>';". PHP_EOL;
			while($row = mysql_fetch_array($zlist)){
				echo( "txt+='<tr><td id=\"zip\">". $row['zipcode'] . "</td><td class=\"area\">" .$row['city'] . "</td><td class=\"area\">" . $row['state'] . "</td><td class=\"cord\">" . $row['lat'] . "</td><td class=\"cord\">" . $row['lon'] . "</td><td>" . round(latLonToMiles($lat1,$lon1,$row['lat'],$row['lon']),2) . "</td><td>" . ($row['timezone']+5) ."</td></tr>';" . PHP_EOL );
			}
			echo "txt+='</table>';" . PHP_EOL;
			echo "txt+='</div>';";
			echo "document.getElementById(\"dummy\").innerHTML=txt;". PHP_EOL;
			echo"</script> ";
		}
		if(isset($_GET["button"]) && $_GET["button"] == "Create DB"){
			createDB();
		}
		if(isset($_GET["button"]) && $_GET["button"] == "Delete DB"){
			deleteDB();
		}
		if(isset($_GET["button"]) && $_GET["button"] == "List Nearby Zip Codes"){
			if($_GET["lat1"] != null && $_GET["lon1"] != null){
				listCodes();
			}
			else{
				echo "<script type=\"text/javascript\">" . PHP_EOL;
				echo "var txt=\"\";" . PHP_EOL;
				echo "txt+='<div id=\"listHolder\">';" . PHP_EOL;
				echo "txt+= 'Please Click on the Map to Select an Area';".PHP_EOL;
				echo "txt+='</div>';";
				echo "document.getElementById(\"dummy\").innerHTML=txt;". PHP_EOL;
				echo"</script> ";
			}
		}
	?>
	
	<script type="text/javascript">   
		function draw(x,y){  
			var canv=document.getElementById("myCanvas");
			var c=canv.getContext("2d");      
			var img = new Image();  
			var w, h;
			
			img.onload = function(){  			  	  
				w=canv.width;		// resize the canvas to the new image size
				h=canv.height;					
				c.drawImage(img, 0, 0, w, h ); 
				if(typeof(x)!='undefined' && typeof(y)!='undefined'){
					c.beginPath();
					c.arc(x,y,5,0,2*Math.PI);
					c.fillStyle = "white";
					c.strokeStyle="white";
					c.fill();
					c.stroke();
					c.closePath();
					c.beginPath();
					c.strokeStyle = "black";
					c.arc(x,y,0.8,0,2*Math.PI);
					c.stroke();
					c.closePath();
					c.beginPath();
					c.arc(x,y,18,0,2*Math.PI);
					c.fillStyle = "rgba(255, 255, 255, 0.25)";
					c.fill();
					c.stroke();
					c.closePath();
				}
			}
		img.src = 'zip_codes_map.png';
		}
		  		  
		function getMousePos(canvas, events){
    		var obj = canvas;
    		var top = 0, left = 0;
			var mX = 0, mY = 0;
			var a,b;
   			while (obj && obj.tagName != 'BODY') { //accumulate offsets up to 'BODY'
        		top += obj.offsetTop;
        		left += obj.offsetLeft;
        		obj = obj.offsetParent;
    		}
    		mX = events.clientX - left + window.pageXOffset;
			a = mX;
    		mY = events.clientY - top + window.pageYOffset;
			b = mY;
			mX = (-126.288404216 + (mX*0.06455390518)).toFixed(4);
			mY = (49.6186161492 - (mY*0.05031132873)).toFixed(4);
    		return { lon: mX, lat: mY, x: a, y: b};
		}
		
		window.onload = function(){
    		var canvas = document.getElementById('myCanvas');
			if(typeof(tempLat)!='undefined'&&typeof(tempLon)!='undefined'){
				var nx = Math.round(Math.abs((tempLon+126.288404216)/0.06455390518));
				var ny = Math.round(Math.abs((tempLat-49.6186161492)/0.05031132873));
				draw(nx, ny);
			}
			else
				draw();
    		canvas.addEventListener('mousedown', function(events){
       		var mousePos = getMousePos(canvas, events);
   		  		var tx = document.getElementById("xpos");
		  		tx.value = mousePos.lon;
    		  	var ty = document.getElementById("ypos");
		  		ty.value = mousePos.lat;
				draw(mousePos.x,mousePos.y);
			}, false);
		}
    </script>  
</body>

</html>