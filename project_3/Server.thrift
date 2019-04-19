struct Server {
    1: i32 ID,
    2: string IP,
    3: string Port
}

struct REQ {
	1: string OP,
	2: string Filename,
	3: string Content,
	4: string ClientIP,
	5: string ClientPort
}