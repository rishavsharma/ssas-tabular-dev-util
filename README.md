# DEV accelerator for SSAS Tabular models
## Initial list of Features
- Build SSAS model from blank model with the imported list of tables
- Creates aliases in the model as per excel 
- Create Hierarchies as per excel
- Create Derived column (calculate or measures) as per excel
- Create relationships as per excel
- Rename tables and column names to business friendly name
- Generate status of actions taken on excel config and output it into new excel which gives line by line status of input excel
- If multiple models are created, the utility can merge these models into one.

## Usage
Utility can generate model from excel config and vice versa
### Things to avoid in Excel Config
- Business names for column should't have '[' or ']'
- Business names for tables shouldn't have '-' or '[' or ']'. Although '-' can be used if the fully qualified names "'TABLE_NAME'[COLUMN_NAME]" is used. Additionally the regular expression need to be changed to cater '-'. 
### Generating model based on excel config
The utility can work on existing models to merge the changes or work on template which is freshly imported list of tables in bim model or can take multiple model already created to work on
```
usage: ModelsExcelImport
 -i,--input <arg>      input .bim file or folder with .bim files to be
                       proccessed
 -t,--template <arg>   SSAS model file(.bim) with freshly imported tables
                       to rebuild the whole model from excel config
 -e,--excel <arg>      input Excel config file
 -n,--rename           rename model based on excel config
 -r,--relation         build relationships based on excel config
 -a,--alias            create aliases based on excel config
 -h,--hierarchy        export hierarchy Columns to excel
 -d,--derived          create derived column (calculated or measures)
                       based on excel config
 -g,--generate         generate combined model
 -o,--output <arg>     folder where output would be writen
 -s,--status           Export the excel with failure status highlighted in
                       each sheet
 -m,--multiple         generate model relationships status in multiple
                       sheets
 -x,--scripts          output TMSL script files to out folder/scripts                       
                       
```

### Generating excel config from existing models

```
usage: ModelsExcelExport
 -i,--input <arg>    input file path or folder where .bim files are placed
 -n,--rename         export renaming columns to excel
 -r,--relation       export relations to excel
 -a,--alias          export aliases to excel
 -d,--derived        export derived Columns to excel
 -h,--hierarchy      export hierarchy Columns to excel
 -g,--generate       generate combined model
 -o,--output <arg>   folder where output would be writen
 -e,--excel <arg>    name of the excel file, don't put path here. use -o
                     for folder
 -m,--multiple       generate model relationships in multiple sheets
```