#!/bin/bash
queries=(
    "finger extension rehab exercise"
    "wrist flexion extension rehab"
    "wrist rotation rehab exercise"
    "thumb opposition rehab"
    "thumb flexion extension rehab"
    "finger flexion rehab"
    "finger pinch rehab exercise"
    "seated knee flexion rehab"
    "hip flexion rehab exercise"
    "hip abduction rehab"
)
for q in "${queries[@]}"; do
    # format query for url
    url_q=$(echo "$q" | sed 's/ /+/g')
    # fetch youtube search and extract first watch?v=
    vid=$(curl -s "https://www.youtube.com/results?search_query=$url_q" | grep -o 'watch?v=[a-zA-Z0-9_-]\{11\}' | head -n 1)
    echo "$q: $vid"
done
