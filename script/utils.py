#!/usr/bin/env python

import argparse
import json
import sys
import numpy as np
import datetime
import time

def read_training_data(fname):
    data = []

    lineno = 0
    for line in open(fname):
        lineno += 1
        try:
            this_line = json.loads(line)
            assert("type" in this_line)
            assert(this_line["type"] in ["training_sample", "empirical"])
            for sample in this_line["samples"]:
                if "scan" not in sample or len(sample["scan"]) == 0:
                    continue

                pattern = dict(this_line)
                pattern["scan"] = sample["scan"]["access_points"]
                del pattern["samples"]
                    
                data.append(pattern)
        except ValueError:
            print >>sys.stderr, "Barfing on line {0}: {1}".format(lineno, line.strip())

    return data

def read_data(fname):
    data = []

    lineno = 0
    time_format = "%a %b %d %H:%M:%S %Z %Y"
    for line in open(fname):
        lineno += 1
        try:
            this_line = json.loads(line)
            this_line["time"] = datetime.datetime(*time.strptime(this_line["time"], time_format)[:6])

            for sample in this_line["samples"]:
                if "scan" not in sample or len(sample["scan"]) == 0:
                    continue

                pattern = dict(this_line)
                pattern.update(sample)
                pattern.update(sample["scan"])
                pattern["scan"] = pattern["access_points"]
                del pattern["samples"]

                data.append(pattern)
        except ValueError:
            print >>sys.stderr, "Bad line number {0}".format(lineno)
    return data

# {"time":"Sun Oct 27 13:00:58 EDT 2013","type":"training_sample","location":1,"samples":[
# {"time":"Sun Oct 27 13:00:58 EDT 2013","type":"training_sample","location":1,"samples":[{"scan_level":3,"scan_time":"Sun Oct 27 12:59:51 EDT 2013","scan":{"access_points":[{"timestamp":337113328229,"bssid":"00:24:6c:d0:79:10","ssid":"EGR Wi-Fi","capabilities":"[ESS]","rssi":-60,"freq":5785}

def parse_training_data(data):
    ret = []
    bssids = []
    rssis = []
    freqs = []
    ssids = []

    for sample in data:
        assert(sample["type"] == "training_sample")
        scans = sample["samples"]
        
        for scan in sample["samples"]:
            assert(scan["scan_level"] == 3)

            if "scan" not in scan:
                print >>sys.stderr, "WARNING: Training sample for location {0} doesn't have any scans {1}".format(sample["location"], scan)
                continue

            training_sample = {}
            training_sample["location"] = sample["location"]

            for ap in scan["scan"]["access_points"]:
                training_sample[ap["bssid"]] = ap["rssi"]
                bssids.append(ap["bssid"])
                rssis.append(ap["rssi"])
                freqs.append(ap["freq"])
                ssids.append(ap["ssid"])

            ret.append(training_sample)

    return ret, bssids, rssis, freqs, ssids

def draw_barchart(plt, data, xlabel, ylabel):
    labels = sorted(set(data))
    idx = np.arange(len(labels))
    counts = dict()
    for label in data:
        counts[label] = counts.get(label, 0) + 1

    plt.bar(idx, [counts[label] for label in labels], 1)
    plt.xlabel(xlabel)
    plt.ylabel(ylabel)

    plt.xticks(idx - .5, labels, rotation=90)

    #plt.autoscale(enable=True, axis='both', tight=True)

def draw_accum(data, xlabel, ylabel):

    labels = list(set(data))
    tmp = dict()
    for d in data:
        tmp[d] = tmp.get(d, 0) + 1

    counts = tmp.values()
    counts.append(0)
    counts.sort(reverse=True)

    plt.plot(counts)
    plt.xlabel(xlabel)
    plt.ylabel(ylabel)
