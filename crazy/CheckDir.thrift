service CheckDir {
	bool ping(),
	void CheckDirectory(1: string DirPath),
	string SubmitTask(1: string file, 2:i32 port),
	string SubmitSorting(1: string interDir, 2:i32 port),
	string CheckFile(1: string path),
	string WriteString(1:string fileName, 2:double poswords, 3:double negwords, 4:double sentiment),
	list<string> getSentimentWords(1: string path),
	string SortIntermediateData(1: string DirPath)
}
