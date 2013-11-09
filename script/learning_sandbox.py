#!/usr/bin/env python

import argparse
import json
import sys
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np

def read_data(fname):
    data = []

    for line in open(fname):
        this_line = json.loads(line)
        assert("type" in this_line)
        assert(this_line["type"] in ["training_sample", "empirical"])
        data.append(this_line)

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

def draw_barchart(data, fname, xlabel, ylabel):
    plt.clf()
    labels = sorted(set(data))
    idx = np.arange(len(labels))

    plt.bar(idx, [data.count(label) for label in labels], 1)
    plt.xlabel(xlabel)
    plt.ylabel(ylabel)

    plt.xticks(idx - .5, labels, rotation=90)

    plt.autoscale(enable=True, axis='both', tight=True)
    plt.savefig(fname, bbox_inches="tight")

def draw_accum(data, fname, xlabel, ylabel):
    plt.clf()
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
    plt.savefig(fname)
    
def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("training_file")
    args = parser.parse_args()

    data = read_data(args.training_file)
    print "Num training= {0}".format(len(data))

    training_patterns, bssids, rssis, freqs, ssids = parse_training_data(data)
    out = open("matrix.txt", "w")

    unique_bssids = sorted(set(bssids))
    print >>out, "{0}\t{1}".format("location", "\t".join(unique_bssids))
    for pattern in training_patterns:
        s = str(pattern["location"])
        for bssid in unique_bssids:
            s += "\t" + str(pattern.get(bssid, "0"))
        print >>out, s

    out.close()

    print "Num patterns= {0}, unique bssids= {1}, unique ssids= {2}".format(len(training_patterns), len(set(bssids)), len(set(ssids)))

    draw_barchart(ssids, "ssid_counts.eps", "ssid", "count")
    draw_accum(bssids, "bssid_counts.eps", "bssid", "count")
    draw_barchart(rssis, "rssi_counts.eps", "rssi", "count")
    draw_barchart(freqs, "freq_counts.eps", "freq", "count")
    

if __name__ == "__main__":
    main()
