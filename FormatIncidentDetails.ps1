param($FileName)
#Function Get-Incident-Details
#{

#    Param 
#  ( 
#           [parameter(position=0)]
#            $FileName
#    )


#Write-Host "Start..!!"

#select excel file you want to read
$file2 = "C:\SAFAL_Automation\RPA projects\PTS_MSGW Alert\1413\MSGW_Connection_Details.xlsx"
$file = $FileName
$sheetName = "IncidentList"

#create new excel COM object
$excel = New-Object -com Excel.Application
$excel2 = New-Object -com Excel.Application

#
$wb2=$excel2.workbooks.open($file2)
$sheet2 = $wb2.Worksheets.Item("ClientInfo")
$rowMax2 = ($sheet2.UsedRange.Rows).Count



#open excel file
$wb = $excel.workbooks.open($file)

#select excel sheet to read data
$sheet = $wb.Worksheets.Item($sheetname)

#select total rows
$rowMax = ($sheet.UsedRange.Rows).Count

$sheet.Cells.Item(2,12).value=($rowMax-1).tostring()


for ($i = 2; $i -le $rowMax; $i++)
{
     
    #read data from each cell
    $celldata = $sheet.Cells.Item($i,4).Text
    #Write-Host $celldata
    if($celldata -match "There is an issue with this job")
    {
        $SplittedArray=$celldata.Split('/')
            #Write-Host $SplittedArray[0]
            #Write-Host $SplittedArray[1]
            #Write-Host $SplittedArray[2]
            
         $Splited1=$SplittedArray[0].Split(" ")
         $Splited2=$SplittedArray[2].Split(" ")
         $size= $Splited1.count -1
            #Write-Host $Splited1[$size]
            #Write-Host $SplittedArray[1]
            #Write-Host $Splited2[0]
         $sheet.Cells.Item($i,9).value=$Splited1[$size]
         $sheet.Cells.Item($i,10).value=$SplittedArray[1]
         $sheet.Cells.Item($i,11).value=$Splited2[0]
         
         $rowNum=$sheet2.Columns.item(1).find($SplittedArray[1]).row

         $sheet.Cells.Item($i,15).value=$sheet2.Cells.Item($rowNum,4).text
         $sheet.Cells.Item($i,16).value=$sheet2.Cells.Item($rowNum,5).text
         $sheet.Cells.Item($i,17).value=$sheet2.Cells.Item($rowNum,6).text
         $sheet.Cells.Item($i,18).value=$sheet2.Cells.Item($rowNum,7).text
         $sheet.Cells.Item($i,19).value=$sheet2.Cells.Item($rowNum,9).text
    }

}

$excel.DisplayAlerts = $false
$excel.ScreenUpdating = $false
$excel.Visible = $false
$excel.UserControl = $false
$excel.Interactive = $false

#$wb. saveas("C:\SAFAL_Automation\RPA projects\PTS_MSGW Alert\Json2.xlsm")
#$excel.ActiveWorkbook.SaveAs("C:\SAFAL_Automation\RPA projects\PTS_MSGW Alert\Json2.xlsm")
#$wb.saveas("C:\SAFAL_Automation\RPA projects\PTS_MSGW Alert\SampleJson1.xlsx",51,$null,$null,$false,$false,"xlNoChange","xlLocalSessionChanges")
$excel.ActiveWorkbook.SaveAs($file)

$wb.close($true)
$excel.Quit()

$wb2.close($true)
$excel2.Quit()

#force stop Excel process
#Stop-Process -Name EXCEL -Force

#}