# GymSocial

**Nome:** Samuel Souza Pereira Nunes  
**Curso:** Sistemas de Informação

## Tema 
O objetivo é criar uma rede social de academia, onde amigos se juntam e montam um grupo e compartilham fotos dos seus treinos. Os grupos podem montar desafios e quem tiver mais check-ins vence. 
Os integrantes do grupo pode postar check-ins, comentar em chek-ins, se conectar com pessoas do grupo, os integrantes do grupo podem conversar em tempo.


## Processo de desenvolvimento
O front-end foi feito em react e o back-end é uma api em javalin, o banco de dados é PostgreSQL, o HikariCP para conexão com o banco e o Flyway para gerenciar os schemas do banco.

## Fluxo de autenticação

A autenticação foi feita em JWT e Refresh Token Rotation.
No react temos esse componente AuthGuard que controla o fluxo de acesso no front-end.
```
import { Dumbbell } from "lucide-react";
import { useEffect, useState } from "react";
import {
  Navigate,
  Outlet,
  useLocation,
} from "react-router-dom";
import { restoreSession } from "../../../services/apiClient";
import { buildAuthPath, getAuthRedirect } from "../services/authRedirect";

type AuthGuardProps = {
  mode: "require" | "guest";
};

type AuthStatus = "checking" | "authenticated" | "guest";

function AuthGuard({ mode }: AuthGuardProps) {
  const location = useLocation();
  const [status, setStatus] = useState<AuthStatus>("checking");

  useEffect(() => {
    let active = true;

    restoreSession()
      .then((authenticated) => {
        if (active) {
          setStatus(authenticated ? "authenticated" : "guest");
        }
      })
      .catch(() => {
        if (active) {
          setStatus("guest");
        }
      });

    return () => {
      active = false;
    };
  }, []);

  if (status === "checking") {
    return <AuthGuardLoading />;
  }

  if (mode === "require" && status === "guest") {
    const currentPath = `${location.pathname}${location.search}`;
    return (
      <Navigate
        to={buildAuthPath("/login", currentPath)}
        replace
      />
    );
  }

  if (mode === "guest" && status === "authenticated") {
    return (
      <Navigate
        to={getAuthRedirect(location.search)}
        replace
      />
    );
  }

  return <Outlet />;
}

export function GuestOnly() {
  return <AuthGuard mode="guest" />;
}

export function RequireAuth() {
  return <AuthGuard mode="require" />;
}

function AuthGuardLoading() {
  return (
    <main className="grid min-h-screen place-items-center bg-zinc-50">
      <div className="text-center">
        <span className="mx-auto grid size-14 animate-pulse place-items-center rounded-2xl bg-brand-600 text-white">
          <Dumbbell size={26} />
        </span>
        <p className="mt-4 text-sm font-bold text-zinc-500">
          Verificando sua sessão...
        </p>
      </div>
    </main>
  );
}
```

Eu usei o React Router para o roteamento no front. Tem rotas que são guest, as rotas de login e registro, e as rotas auth, que o usuário só pode acessar se estiver autenticado, as rotas require. O componente também proíbe o usuário já logado entrar nas rotas guest. Se o usuário não autenticado tentar entrar em uma rota require, ele é redirecionado para login, e se o usuário autenticado tentar acessar uma rota guest, ele volta para a rota que ele estava antes.

### O fluxo foi pensando e implementado dessa maneira:

Temos o apiClient, que intercepta todas as requests. Ela é responsável por colocar o accessToken no header das requisições e ele também faz a request de refresh para quando o accessToken expira e é necessário criar outro. O accessToken é enviado nos headers de toda requisição, exceto nas requests de login, register e refresh. Nessas requests, o accessToken é enviado no response e é salvo em memória. Eu já tinha implementado autenticações JWT antes, porém com algumas diferenças dessa implementação nesse projeto. Acredito que a implementação de autenticação nesse projeto foi a melhor que eu fiz até o momento, porque nas outras implementações o accessToken era salvo no localStorage e o refreshToken não sofria rotação. Agora o accessToken é salvo em memória, o refreshToken é salvo nos cookies com httpOnly, assim evita que algum código JS malicioso pegue o refreshToken e, além disso, o refreshToken sofre rotação, ou seja, quando o refreshToken é usado mais de uma vez, é criado outro refreshToken e esse novo é o que passa a valer. Eu tive conhecimento da existência do refreshToken rotation criando esse projeto. Confesso que, de início, fiquei meio relutante com a ideia porque, na minha cabeça, não tinha tanto sentido ser jogado fora tão rapidamente assim, ele deveria ter um tempo de expiração maior e ser usado várias e várias vezes para gerar um novo accessToken. Mas, lendo em fóruns e pesquisando, vi que a rotação é algo válido e mais seguro, já que, se um atacante descobre o refreshToken, o poder de ataque diminui drasticamente, porque logo logo ele vai deixar de ser válido. Sem a rotação, o atacante poderia renovar a sessão dele várias e várias vezes.

## URL 
https://final-2026a-samuel.vercel.app/

Rede social para grupos de amigos registrarem exercícios, participarem de
desafios, comentarem check-ins e conversarem em tempo real.

## Desenvolvimento local

1. Copie `.env.example` para `.env`.
2. Execute:

```bash
docker compose up --build
```

3. Em outro terminal:

```bash
cd frontend
yarn install
yarn dev
```

Frontend: http://localhost:5173

API: http://localhost:7000

Chat: http://localhost:7001
