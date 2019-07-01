#!/bin/bash

for i in {0..150..10}
    do
        if [ "$i" -eq "0" ]; then
            i=1
        fi
        sed -e 's/TEMPLATE_SIZE/'$(($i+8))'/' < saturation_files/configs/config-2pc.template > saturation_files/configs/config.config
        java -classpath ./out/production/Sim:./jep-2.3.0.jar:./djep-1.0.0.jar peersim.Simulator saturation_files/configs/config.config >> saturation_files/results/c13a84107a50d3cb417359d94d671b8b190ae20a/tpc_data.csv 2>/dev/null
        echo ', '$(($i)) >> saturation_files/results/c13a84107a50d3cb417359d94d671b8b190ae20a/tpc_data.csv
        rm saturation_files/configs/config.config
        for j in {0..30..5}
            do
                if [ "$j" -eq "0" ]; then
                    j=1
                fi
                sed -e 's/TEMPLATE_SIZE/'$(($i+9))'/' -e 's/BATCH_SIZE/'$j'/' < saturation_files/configs/config-tsb.template > saturation_files/configs/config.config
                java -classpath ./out/production/Sim:./jep-2.3.0.jar:./djep-1.0.0.jar peersim.Simulator saturation_files/configs/config.config >> saturation_files/results/c13a84107a50d3cb417359d94d671b8b190ae20a/tsb_data_$j.csv 2>/dev/null
                echo ', '$(($i)) >> saturation_files/results/c13a84107a50d3cb417359d94d671b8b190ae20a/tsb_data_$j.csv
                rm saturation_files/configs/config.config

            done

    done