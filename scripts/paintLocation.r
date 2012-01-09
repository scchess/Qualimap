if(!require("optparse")) { install.packages("optparse", repos = "http://cran.r-project.org") }
if(!require("XML")) { install.packages("XML", repos = "http://cran.r-project.org") }

suppressPackageStartupMessages(library("optparse"))
if(!require("Repitools")) {
    source("http://bioconductor.org/biocLite.R")
    biocLite("Repitools")
}
if(!require("Rsamtools")) {
    source("http://bioconductor.org/biocLite.R")
    biocLite("Rsamtools")
}
library(Repitools)
library(Rsamtools)
library(XML)

option_list <- list(
                 make_option(c("-v", "--verbose"), action="store_true", default=TRUE,
                         help="Print extra output [default]"),
                 make_option(c("-q", "--quietly"), action="store_false",
                         dest="verbose", help="Print little output"),
                 make_option(c("--fileConfig"), type="character", action="store",
                         help="REQUIRED. Path to configuartion file.",
                         metavar="file_counts"),
                 make_option("--homedir", type="character", action="store",
                           help="Option allows to launch the script from external tools.", default="./",
                           metavar="home_dir folder")
                 )

opt <- parse_args(OptionParser(option_list=option_list))

HOMEDIR <- opt$homedir

source(file.path(HOMEDIR, "parseConfig.r"))
source(file.path(HOMEDIR, "utils.r"))

print("STATUS:Parsing arguments:")

args <- parseConfig(opt$fileConfig)
print(colnames(args))
print(args[[1,"microarray"]])

sample1 <- unParseDF(args[[1,"sample1"]], c("medips","input","sample.name","replicate.name"))
print(sample1)
print(class(sample1))
sample2 <- unParseDF(args[[1,"sample2"]], c("medips","input","sample.name","replicate.name"))
print(sample2)
geneSelection <- if(!is.na(args[[1,"geneSelection"]])) unParseDF(args[[1,"geneSelection"]], c("file","column")) else NULL
print(geneSelection)
ma <- if(!is.na(args[[1,"microarray"]])) unParseDF(args[[1,"microarray"]],c("file","threshold")) else NULL
ma.file <- if(!is.null(ma)) as.character(ma[1,"file"]) else NULL 
ma.threshold <- if(!is.null(ma)) as.numeric(ma[,"threshold"]) else c(0) 
print(ma)
clusters <- if(!is.na(args[[1,"clusters"]])) unParseDF(args[[1,"clusters"]],c("num")) else c(25) # Default 23 clusters
print(clusters)
dirOut <- if(!is.na(args[[1,"dirOut"]])) unParseDF(args[[1,"dirOut"]], c("dirOut"))[1,1] else NULL # [1,1] because it has only one value
print(dirOut)
dir.create(dirOut,showWarnings = FALSE)

expID <- if(!is.na(args[[1,"expID"]])) unParseDF(args[[1,"expID"]], c("expID"))[1,1] else "location" # [1,1] because it has only one value. Default "location"
print(expID)

print("location")
print(args[[1,"location"]])

up=if(!is.na(args[[1,"location"]])) as.numeric(unParseDF(args[[1,"location"]],c("up"))[1,1]) else 2000
print("up")
print(up)

down=if(!is.na(args[[1,"location"]])) as.numeric(unParseDF(args[[1,"location"]],c("down"))[1,1]) else 500
print("down")
print(down)

freq=if(!is.na(args[[1,"location"]])) as.numeric(unParseDF(args[[1,"location"]],c("freq"))[1,1]) else 100
print("freq")
print(freq)

print("fragment")
print(args[[1,"fragment"]])
fragment=if(!is.na(args[[1,"fragment"]])) as.numeric(unParseDF(args[[1,"fragment"]],c("fragment"))[1,1]) else 300
print("fragment")
print(fragment)

print("STATUS:Building annotations:")
annot <- buildAnnotation(geneSelection=geneSelection)
#annot <- buildAnnotation()

#print(c(as.character(sample1[,"medips"]), as.character(sample2[1,"medips"])))
#annot <- 1
print("STATUS:Building features scores:")
fs <- buildFeatureScores(sample1=sample1,sample2=sample2,annot=annot,up=up,down=down,freq=freq,s.width=fragment)
#fs <- buildFeatureScores(grl.stored="/data/medips/analysis/location/Rdata/GRangesList.48.Rdata",annot=annot)
#cl.from.fs <- heatmapCluster(featureScores=fs,ma.file=ma.file,expr.threshold=c(as.numeric(ma.threshold)),n.clusters=c(as.numeric(clusters[1,])),dirOut=dirOut,expName=expID,up=up,down=down,freq=freq,s.width=fragment)
print("STATUS:Perform clustering:")
cl.from.fs <- heatmapCluster(featureScores=fs,ma.file=ma.file,expr.threshold=c(as.numeric(ma.threshold)),n.clusters=c(as.numeric(clusters[1,])),dirOut=dirOut,expName=expID)

#fs <- buildFeatureScores2(sample1=sa:mple1,sample2=sample2,annot=annot)

