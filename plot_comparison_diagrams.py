#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Create comparable result diagrams for EvoSuite-GWO, Zest, Gemini, and DeepSeek
from the project_sqa folder. Supports recursively discovered CSV files and
EvoSuite-GWO terminal.log files. New method folders can be added without
changing the script, provided their CSV columns are compatible.
"""
from pathlib import Path
import re
import warnings
import pandas as pd
import matplotlib.pyplot as plt

ROOT = Path(__file__).resolve().parent
OUT = ROOT / "Results_Diagrams"
PER_PROJECT = OUT / "Per_Project"
METHOD_SUMMARY = OUT / "Method_Summary"
COMPARISON = OUT / "Comparison"

METHOD_ALIASES = {
    "gwo": "EvoSuite-GWO", "greywolfoptimizer": "EvoSuite-GWO",
    "evosuite-gwo": "EvoSuite-GWO", "evosuite_gwo": "EvoSuite-GWO",
    "zest": "Zest",
    "gemini": "Gemini-3.7-Flash", "gemini-3.7-flash": "Gemini-3.7-Flash",
    "deepseek": "DeepSeek-V4-Flash", "deepseek-v4-flash": "DeepSeek-V4-Flash",
}
METHOD_ORDER = ["EvoSuite-GWO", "Zest", "Gemini-3.7-Flash", "DeepSeek-V4-Flash"]

def method_from_path(path, model=""):
    text = (str(path) + " " + str(model)).lower()
    for key, value in METHOD_ALIASES.items():
        if key in text:
            return value
    return None

def num(value):
    try:
        if pd.isna(value) or str(value).strip().upper() in {"N/A", "", "NONE"}:
            return float("nan")
        return float(str(value).replace("%", "").strip())
    except (TypeError, ValueError):
        return float("nan")

def as_bool(value):
    if isinstance(value, bool):
        return value
    s = str(value).strip().lower()
    if s in {"true", "1", "yes", "y"}: return True
    if s in {"false", "0", "no", "n"}: return False
    return None

def normalize_csv(path):
    try:
        df = pd.read_csv(path)
    except Exception as e:
        warnings.warn(f"Skip unreadable CSV {path}: {e}")
        return []
    if df.empty:
        return []
    cols = {c.lower().strip(): c for c in df.columns}
    def get(row, *names, default=None):
        for name in names:
            col = cols.get(name.lower())
            if col is not None:
                return row.get(col, default)
        return default

    records = []
    for _, row in df.iterrows():
        model = get(row, "model", "method", default="")
        method = method_from_path(path, model)
        if not method:
            continue
        project = str(get(row, "project", default="")).strip()
        if not project or project.lower() == "nan":
            project = next((part for part in path.parts if part in {
                "Chart","Cli","Closure","Codec","Collections","Compress","Csv",
                "Gson","JacksonCore","JacksonDatabind","JacksonXml","Jsoup",
                "JxPath","Lang","Math","Mockito","Time"}), "Unknown")
        bug = str(get(row, "bug_id", "bug", default="")).strip()
        bug = re.sub(r"^[A-Za-z]+-", "", bug)
        records.append({
            "project": project, "bug_id": bug, "method": method,
            "class_name": get(row, "class_name", "target_class", default=""),
            "compile_ok": as_bool(get(row, "compile_ok", "compile_success")),
            "line_coverage": num(get(row, "line_coverage", "line_cov")),
            "branch_coverage": num(get(row, "branch_coverage", "condition_coverage", "branch_cov")),
            "tokens_used": num(get(row, "tokens_used", "token_usage")),
            "fault_detected": as_bool(get(row, "fault_detected", "fault_found")),
            "time_sec": num(get(row, "time_sec", "execution_time", "duration_sec", "time")),
            "test_cases": num(get(row, "test_cases", "tests", "number_of_tests")),
            "statements": num(get(row, "statements", "statement_count")),
            "test_length": num(get(row, "test_length", "total_length", "length")),
            "mutation_score": num(get(row, "mutation_score", "mutation")),
            "source_file": str(path),
        })
    return records

def parse_gwo_log(path):
    # Expected: .../Result_Round1/<Project>/<Project>-<Bug>b/GWO/<class>/terminal.log
    bug_dir = next((parent for parent in path.parents if re.match(r".+-\d+[ab]$", parent.name, re.I)), None)
    if bug_dir is None:
        return None
    project = bug_dir.parent.name
    bug_match = re.search(r"-(\d+[ab])$", bug_dir.name, re.I)
    bug = bug_match.group(1) if bug_match else bug_dir.name
    try:
        txt = path.read_text(errors="ignore")
    except Exception:
        return None

    def last(pattern, cast=float):
        matches = re.findall(pattern, txt, re.I | re.M)
        if not matches: return float("nan")
        try: return cast(matches[-1])
        except (ValueError, TypeError): return float("nan")

    # Prefer final coverage-analysis values over live progress percentages.
    line = last(r"Coverage of criterion LINE:\s*([\d.]+)%")
    branch = last(r"Coverage of criterion BRANCH:\s*([\d.]+)%")
    mutation = last(r"mutation score:\s*([\d.]+)%")
    runtime = last(r"Search finished after\s*([\d.]+)s")
    statements = last(r"Search finished after\s*[\d.]+s and\s*\d+\s*generations?,\s*([\d,]+)\s*statements", lambda x: float(x.replace(",","")))
    generations = last(r"Search finished after\s*[\d.]+s and\s*(\d+)\s*generations?", int)
    tests = last(r"Generated\s*(\d+)\s*tests", int)
    length = last(r"Generated\s*\d+\s*tests with total length\s*(\d+)", int)
    compile_ok = True if ("Done!" in txt and "Writing JUnit test case" in txt) else (
        False if re.search(r"compilation failed|failed to compile|compile error", txt, re.I) else None)
    class_match = re.search(r"Generating tests for class\s+([^\s]+)", txt)
    return {
        "project": project, "bug_id": bug, "method": "EvoSuite-GWO",
        "class_name": class_match.group(1) if class_match else path.parent.name,
        "compile_ok": compile_ok, "line_coverage": line, "branch_coverage": branch,
        "tokens_used": float("nan"), "fault_detected": None, "time_sec": runtime,
        "test_cases": tests, "statements": statements, "test_length": length,
        "mutation_score": mutation, "generations": generations, "source_file": str(path),
    }

def load_all():
    records = []
    for csv_path in ROOT.rglob("*.csv"):
        if OUT in csv_path.parents:
            continue
        records.extend(normalize_csv(csv_path))
    for log in ROOT.rglob("terminal.log"):
        if "GreyWolfOptimizer" not in str(log):
            continue
        item = parse_gwo_log(log)
        if item:
            records.append(item)
    if not records:
        return pd.DataFrame()
    df = pd.DataFrame(records)
    for c in ["line_coverage","branch_coverage","tokens_used","time_sec","test_cases",
              "statements","test_length","mutation_score"]:
        df[c] = pd.to_numeric(df[c], errors="coerce")
    return df

def ordered_methods(values):
    present = list(pd.Series(values).dropna().unique())
    return [m for m in METHOD_ORDER if m in present] + [m for m in present if m not in METHOD_ORDER]

def save_bar(data, x, y, title, ylabel, file, methods=None, ylim=None):
    data = data.dropna(subset=[y])
    if data.empty: return
    if methods is None: methods = ordered_methods(data["method"])
    means = data.groupby("method")[y].mean().reindex(methods).dropna()
    if means.empty: return
    fig, ax = plt.subplots(figsize=(10, 5.5))
    means.plot(kind="bar", ax=ax)
    ax.set_title(title)
    ax.set_xlabel("")
    ax.set_ylabel(ylabel)
    ax.tick_params(axis="x", rotation=15)
    if ylim: ax.set_ylim(*ylim)
    ax.grid(axis="y", alpha=.25)
    for cont in ax.containers:
        ax.bar_label(cont, fmt="%.1f", padding=3, fontsize=8)
    fig.tight_layout()
    file.parent.mkdir(parents=True, exist_ok=True)
    fig.savefig(file, dpi=180)
    plt.close(fig)

def save_coverage_grouped(data, title, file):
    metrics = [("line_coverage","Line"),("branch_coverage","Branch")]
    means = data.groupby("method")[[m[0] for m in metrics]].mean()
    methods = ordered_methods(means.index)
    means = means.reindex(methods).dropna(how="all")
    if means.empty: return
    fig, ax = plt.subplots(figsize=(10,5.5))
    means.rename(columns={"line_coverage":"Line Coverage","branch_coverage":"Branch Coverage"}).plot(
        kind="bar", ax=ax, width=.78)
    ax.set_title(title); ax.set_xlabel(""); ax.set_ylabel("Coverage (%)")
    ax.set_ylim(0,100); ax.tick_params(axis="x",rotation=15)
    ax.grid(axis="y",alpha=.25); ax.legend()
    fig.tight_layout(); file.parent.mkdir(parents=True,exist_ok=True)
    fig.savefig(file,dpi=180); plt.close(fig)

def save_box(data, metric, title, file):
    d = data.dropna(subset=[metric])
    methods = ordered_methods(d["method"])
    vals = [d.loc[d.method==m,metric].values for m in methods]
    vals = [v for v in vals if len(v)]
    labels = [m for m in methods if len(d.loc[d.method==m,metric])]
    if not vals: return
    fig,ax=plt.subplots(figsize=(10,5.5))
    ax.boxplot(vals,tick_labels=labels,showfliers=True)
    ax.set_title(title); ax.set_ylabel("Coverage (%)"); ax.set_ylim(0,100)
    ax.tick_params(axis="x",rotation=15); ax.grid(axis="y",alpha=.25)
    fig.tight_layout(); file.parent.mkdir(parents=True,exist_ok=True)
    fig.savefig(file,dpi=180); plt.close(fig)

def main():
    for folder in [PER_PROJECT,METHOD_SUMMARY,COMPARISON]:
        folder.mkdir(parents=True,exist_ok=True)
    df=load_all()
    if df.empty:
        print("No compatible result CSV or EvoSuite-GWO terminal.log files found.")
        print("Place this script in project_sqa and run it from there.")
        return
    df.to_csv(OUT/"all_methods_normalized.csv",index=False)

    # Project-level: consistent chart set for every project with data.
    for project, d in df.groupby("project"):
        dest=PER_PROJECT/str(project)
        save_coverage_grouped(d,f"{project}: Line & Branch Coverage",dest/"coverage_comparison.png")
        save_box(d,"line_coverage",f"{project}: Line Coverage Distribution",dest/"line_coverage_boxplot.png")
        save_bar(d,"method","fault_detected",f"{project}: Fault Detection Rate (%)",
                 "Fault Detection (%)",dest/"fault_detection.png",
                 ylim=(0,100))
        save_bar(d,"method","time_sec",f"{project}: Execution Time",
                 "Time (seconds)",dest/"execution_time.png")
        save_bar(d,"method","test_cases",f"{project}: Number of Test Cases",
                 "Test Cases",dest/"test_cases.png")
        save_bar(d,"method","mutation_score",f"{project}: Mutation Score",
                 "Mutation Score (%)",dest/"mutation_score.png",ylim=(0,100))
        # Save project data for audit and future plots.
        d.to_csv(dest/"results_data.csv",index=False)

    # Per-method summary across all available project results.
    summary=df.groupby("method").agg(
        result_rows=("method","size"),
        projects=("project","nunique"),
        compile_success_rate=("compile_ok",lambda s:s.dropna().astype(bool).mean()*100 if s.notna().any() else float("nan")),
        mean_line_coverage=("line_coverage","mean"),
        median_line_coverage=("line_coverage","median"),
        mean_branch_coverage=("branch_coverage","mean"),
        mean_fault_detection=("fault_detected",lambda s:s.dropna().astype(bool).mean()*100 if s.notna().any() else float("nan")),
        mean_time_sec=("time_sec","mean"),
        mean_test_cases=("test_cases","mean"),
        mean_test_length=("test_length","mean"),
        mean_mutation_score=("mutation_score","mean"),
        mean_tokens=("tokens_used","mean"),
    ).reset_index()
    summary.to_csv(METHOD_SUMMARY/"method_summary.csv",index=False)
    for method,d in df.groupby("method"):
        dest=METHOD_SUMMARY/method
        d.to_csv(dest/"results_data.csv",index=False)
        save_bar(d,"method","line_coverage",f"{method}: Line Coverage by Result",
                 "Line Coverage (%)",dest/"line_coverage.png",ylim=(0,100))
        save_bar(d,"method","branch_coverage",f"{method}: Branch Coverage by Result",
                 "Branch Coverage (%)",dest/"branch_coverage.png",ylim=(0,100))

    # Overall method comparison, weighted as mean of available result rows.
    save_coverage_grouped(df,"Overall: Line & Branch Coverage",COMPARISON/"overall_coverage.png")
    save_box(df,"line_coverage","Overall: Line Coverage Distribution",COMPARISON/"overall_line_coverage_boxplot.png")
    save_bar(df,"method","fault_detected","Overall: Fault Detection Rate (%)",
             "Fault Detection (%)",COMPARISON/"overall_fault_detection.png",ylim=(0,100))
    save_bar(df,"method","time_sec","Overall: Execution Time",
             "Time (seconds)",COMPARISON/"overall_execution_time.png")
    save_bar(df,"method","test_cases","Overall: Test Suite Size",
             "Number of Test Cases",COMPARISON/"overall_test_cases.png")
    save_bar(df,"method","mutation_score","Overall: Mutation Score",
             "Mutation Score (%)",COMPARISON/"overall_mutation_score.png",ylim=(0,100))
    llm=df[df.method.isin(["Gemini-3.7-Flash","DeepSeek-V4-Flash"])]
    save_bar(llm,"method","tokens_used","LLM Token Usage",
             "Tokens",COMPARISON/"llm_token_usage.png")
    # Heatmap with rows as project-bug and columns as method.
    heat=df.dropna(subset=["line_coverage"]).copy()
    if not heat.empty:
        heat["case"]=heat["project"].astype(str)+"-"+heat["bug_id"].astype(str)
        pivot=heat.pivot_table(index="case",columns="method",values="line_coverage",aggfunc="mean")
        pivot=pivot.reindex(columns=ordered_methods(pivot.columns))
        fig_h=max(5,min(22,0.28*len(pivot)+2))
        fig,ax=plt.subplots(figsize=(10,fig_h))
        im=ax.imshow(pivot.values,aspect="auto",vmin=0,vmax=100)
        ax.set_xticks(range(len(pivot.columns)),labels=pivot.columns,rotation=20,ha="right")
        ax.set_yticks(range(len(pivot.index)),labels=pivot.index,fontsize=7)
        ax.set_title("Line Coverage Heatmap: Project / Bug × Method")
        fig.colorbar(im,ax=ax,label="Line Coverage (%)")
        fig.tight_layout(); fig.savefig(COMPARISON/"line_coverage_heatmap.png",dpi=180); plt.close(fig)
    print(f"Processed {len(df)} result rows from {df.project.nunique()} projects.")
    print(f"Methods found: {', '.join(ordered_methods(df.method))}")
    print(f"Output: {OUT}")
    print("Note: fault detection is plotted only where source CSV provides fault_detected.")
    print("Note: verify Zest CSV schema and GWO terminal.log parsing before final reporting.")

if __name__=="__main__":
    main()
