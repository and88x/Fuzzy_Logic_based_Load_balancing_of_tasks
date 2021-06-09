# Libraries
library(data.table)
library(dplyr)
library(ggplot2)
library(knitr)
library(ggpubr)
#
# Read the files and set some values as factors
dataVM <- fread("./Databases_from_responses/Hosts_responces.csv")
dataT  <- fread("./Databases_from_responses/VM_responces.csv")
#
dataVM$Algorithm  <- as.factor(dataVM$Algorithm)
dataVM$Experiment <- as.factor(dataVM$Experiment)
#
dataT$Algorithm  <- as.factor(dataT$Algorithm)
dataT$Experiment <- as.factor(dataT$Experiment)
#
my_colors <- RColorBrewer::brewer.pal(9, "Set1")
#
dataM <- mutate(dataT, RT = RT *100*2000/ (Tasks*Length), Makespan = Makespan *100*2000/ (Tasks*Length))
#
text_size = 12
#
trim_title <- function(text, n=40){
    paste(strwrap(text, width = n), collapse = "\n")
}
#
two_images = theme(
    legend.position = "none", 
    axis.title.x    = element_text(size = 14),
    axis.text.x     = element_text(size = 12),
    axis.text.y     = element_text(size = 12),
    axis.title.y    = element_text(size = 14),
    legend.text     = element_text(size = 14),
    axis.title      = element_text(size = 16))
#
two_images_small = theme(
    legend.position = "top", 
    axis.title.x    = element_text(size = 14),
    axis.text.x     = element_text(size = 12),
    axis.text.y     = element_text(size = 10),
    axis.title.y    = element_text(size = 14),
    legend.text     = element_text(size = 10),
    axis.title      = element_text(size = 15))